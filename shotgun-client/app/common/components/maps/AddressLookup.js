import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withExternalState, ReduxRouter, Route, connect } from 'custom-redux';
import moment from 'moment';
import { Text, Button, Header, Left, Body, Container, Title, Input, Tab, Row, List, ListItem, View, Content} from 'native-base';
import { getDaoState, getNavigationProps } from 'common/dao';
import { parseGooglePlacesData } from 'common/components/maps/MapUtils';
import {ErrorRegion, LoadingScreen, Icon, Tabs} from 'common/components';
import {debounce} from 'lodash';
import shotgun from 'native-base-theme/variables/shotgun';
import {addressToText} from 'common/utils';
import Logger from 'common/Logger';
import {Keyboard} from 'react-native';
const MAX_RECENT_ADDRESSES = 10;


class AddressLookup extends Component {
  static propTypes = {
    client: PropTypes.object,
    addressLabel: PropTypes.string.isRequired,
    addressPath: PropTypes.array.isRequired,
  };

  constructor(props) {
    super(props);
    this.onAddressSelected = this.onAddressSelected.bind(this);
    this.searchAutoCompleteSuggestions = this.searchAutoCompleteSuggestions.bind(this);
    this.onAddressChanged = this.onAddressChanged.bind(this);
    this.goToTabNamed = this.goToTabNamed.bind(this);
    this.state = {
      busy: false,
      addressSearchText: undefined,
      suggestedPlaces: [],
      hasLookedUpAddresses: false,
      errors: undefined
    };
  }

  onAddressChanged(value){
    this.setState({ addressSearchText: value }, () => this.searchAutoCompleteSuggestions(value, () => this.goToTabNamed('Suggested')));
  }

  componentWillReceiveProps(newProps){
    this.goToTabNamed(newProps.selectedTab, newProps);
  }

  onNavigateAway(){
    Keyboard.dismiss();
  }

  goToTabNamed(name, propOverride){
    const {history, path, addressLabel, addressPath} = propOverride || this.props;
    if (!history.location.pathname.endsWith(name)){
      history.replace({pathname: `${path}/${name}`, state: {addressLabel, addressPath}});
    }
  }

  async searchAutoCompleteSuggestions(value, continueWith){
    const {client, me = {}} = this.props;
    try {
      if (this.pendingAutoCompletePromise){
        this.pendingAutoCompletePromise.cancel();
      }
      if (!value){
        Logger.info(`Clearing results for empty search term ${value}`);
        this.setState({
          suggestedPlaces: undefined,
          hasLookedUpAddresses: false,
          busy: false
        });
        return;
      }
      this.setState({ busy: true, hasLookedUpAddresses: true});
      this.pendingAutoCompletePromise =  client.invokeJSONCommand('mapsController', 'makeAutoCompleteRequest', {
        lat: me.latitude,
        lng: me.longitude,
        input: value,
        language: 'en'
      });
      const responseJSON = await this.pendingAutoCompletePromise;

      const filteredPredictions = responseJSON.predictions;

      Logger.info(`Got ${filteredPredictions.length} results... for search term ${value}`);
      this.setState({
        suggestedPlaces: [...filteredPredictions],
        hasLookedUpAddresses: true,
        busy: false
      }, continueWith);
    } catch (error) {
      this.setState({ error });
    }
  }

  async onAddressSelected(value){
    const {addressPath, setStateWithPath, history, dispatch} = this.props;
    if (!addressPath){
      throw new Error('This control needs an address path');
    }
    setStateWithPath(value, addressPath, history.goBack, dispatch);
  }

  render() {
    const { deliveryAddresses = [], addressLabel, homeAddress, history, height, width, path, selectedTabIndex, client, tabs, me, addressSearchText, suggestedPlaces = [], errors, hasLookedUpAddresses, busy, addressPath} = this.props;
    const {onAddressChanged, onAddressSelected} = this;
    return (
      <Container>
        <Header withButton>
          <Left>
            <Button>
              <Icon name='cross' onPress={() => history.goBack()} />
            </Button>
          </Left>
          <Body><Title style={styles.title}>{addressLabel}</Title></Body>
        </Header>
        <Content keyboardShouldPersistTaps="always">
          <Row size={10} style={{...styles.searchContainer, paddingTop: 10, paddingBottom: 10}}>
            <ErrorRegion errors={errors}>
              <Icon name="pin" paddedIcon originPin style={{ alignSelf: 'center' }} />
              <Input placeholder={addressLabel} style={styles.input} value={addressSearchText} autoFocus={true} onChangeText={onAddressChanged} />
            </ErrorRegion>
          </Row>
          {homeAddress ? <HomeAddressItem address={homeAddress} onAddressSelected={onAddressSelected}/> : null}
          {tabs.length ? <Tabs initialPage={selectedTabIndex}  page={selectedTabIndex} {...shotgun.tabsStyle} >
            {tabs.map(tab =>  <Tab key={tab} heading={tab} onPress={() => this.goToTabNamed(tab)}/>)}
          </Tabs> : null}
          {tabs.length ? <ReduxRouter style={{padding: 10}} client={client} myLocation={me} onAddressSelected={onAddressSelected}  path={path} name="AddressLookupRouter"  height={height - 150} width={width} defaultRoute={{pathname: `${tabs[0]}`, state: {addressLabel, addressPath }}}  >
            <Route path={'Recent'} component={RecentAddresses} {...{deliveryAddresses}}/>
            <Route path={'Suggested'} component={SuggestedAddresses} {...{hasLookedUpAddresses, suggestedPlaces, addressSearchText, busy} }/>
            <Route path={'Nearby Places'} component={ReverseGeoAddresses} />
          </ReduxRouter> : null}
        </Content>
      </Container>
    );
  }
}


const HomeAddressItem = ({address = {}, onAddressSelected}) =>
  <Row style={{paddingLeft: 23}} onPress={() => onAddressSelected(address)}>
    <Icon name="location" paddedIcon originPin style={{ alignSelf: 'flex-start', marginTop: 6 }} />
    <View >
      <Text style={{...styles.addressText, paddingBottom: 6}}>Home</Text>
      <Text style={styles.smallText}>{`${address.line1}, ${address.postCode}`}</Text>
    </View>
  </Row>;


class RecentAddresses extends Component{
  constructor(props){
    super(props);
  }

  render(){
    const {deliveryAddresses, onAddressSelected} = this.props;
    return deliveryAddresses && deliveryAddresses.length ? <View paddedLeft style={styles.resultsContainer}>
      <Text style={styles.smallText}>Recent addresses you have used</Text>
      <List>
        {getOrderedAddresses(deliveryAddresses).map((ad, idx) => <Address address={ad} key={idx} onAddressSelected={onAddressSelected}/>)}
      </List>
    </View> : <Text>No recent addresses</Text>;
  }
}

const Address = ({address = {}, onAddressSelected}) => <ListItem paddedTopBottom onPress={() => onAddressSelected(address)}>
  <View>
    <Text style={styles.addressText}>{addressToText(address)}</Text>
    <Text style={styles.smallText}>{address.city}</Text>
  </View>
</ListItem>;


class SuggestedAddresses extends Component{
  constructor(props){
    super(props);
    this.onSuggestedPlaceSelected = this.onSuggestedPlaceSelected.bind(this);
    this.state = {
      errors: undefined
    };
  }

  async onSuggestedPlaceSelected(rowData){
    try {
      const {client, onAddressSelected} = this.props;
      const res = await client.invokeJSONCommand('mapsController', 'mapPlaceRequest', {
        placeid: rowData.place_id,
        language: 'en'
      }).timeoutWithError(5000, 'Place request timed out');

      await onAddressSelected(parseGooglePlacesData(res.result));
    } catch (error) {
      super.setState({ error });
    }
  }

  render(){
    const {onSuggestedPlaceSelected} = this;
    const {suggestedPlaces, hasLookedUpAddresses, busy, addressSearchText} = this.props;
    const {errors} = this.state;
    if (busy){
      return <LoadingScreen text={`Looking up results for ${addressSearchText}...`}/>;
    }
    return hasLookedUpAddresses ? <ErrorRegion errors={errors}><View  paddedLeft style={{paddingTop: 15}}>
      <Text style={styles.smallText}>{suggestedPlaces.length ? `${suggestedPlaces.length > 1 ? 'Addresses' : 'Address'} matching "${addressSearchText}"` : `No Results Found matching "${addressSearchText}"`}</Text>
      <List>{suggestedPlaces.map((r, i) => <SuggestedPlace key={i} result={r} onSuggestedPlaceSelected={onSuggestedPlaceSelected}/>)}</List>
    </View></ErrorRegion>  : null;
  }
}

const SuggestedPlace = ({result, onSuggestedPlaceSelected}) => {
  return <ListItem paddedTopBottom onPress={() => onSuggestedPlaceSelected(result)}>
    <View>
      <Text style={styles.addressText}>{result.structured_formatting.main_text}</Text>
      <Text style={styles.smallText}>{result.structured_formatting.secondary_text}</Text>
    </View>
  </ListItem>;
};

class ReverseGeoAddresses extends Component{
  constructor(props){
    super(props);
    this.reverseGeoCodeSearch = this.reverseGeoCodeSearch.bind(this);
    this.state = {
      hasLookedUp: false,
      reverseLookedUpAddresses: [],
      busy: false
    };
  }

  beforeNavigateTo(){
    this.reverseGeoCodeSearch();
  }

  async reverseGeoCodeSearch(){
    const {client, myLocation} = this.props;
    const {latitude, longitude} = myLocation;
    try {
      super.setState({
        hasLookedUp: false,
        reverseLookedUpAddresses: [],
        busy: true });
      const reverseLookedUpAddresses = await client.invokeJSONCommand('mapsController', 'getAddressesFromLatLong', {
        latitude, longitude
      });

      super.setState({
        reverseLookedUpAddresses,
        hasReverseGeoAddresses: true,
        suggestedPlaces: undefined,
        busy: false
      });
    } catch (error) {
      super.setState({ error });
    }
  }

  render(){
    const {onAddressSelected} = this.props;
    const {reverseLookedUpAddresses = [], busy} = this.state;
    if (busy){
      return <LoadingScreen text={'Looking for places close to you...'}/>;
    }
    return reverseLookedUpAddresses.length > 0 ? <View paddedLeft style={{paddingTop: 15}}>
      <Text style={styles.smallText}>Places close to current location</Text>
      <List>
        {reverseLookedUpAddresses.map((ad, idx) => <Address address={ad} key={idx} onAddressSelected={onAddressSelected}/>)}
      </List>
    </View> : null;
  }
}

const styles = {
  input: {
  },
  title: {
    fontSize: 20,
  },
  searchContainer: {
    paddingHorizontal: 8,
    backgroundColor: '#FFFFFF',
    borderRadius: 4,
    margin: 10,
    paddingLeft: shotgun.contentPadding,
    padding: 10,
    borderWidth: 0.5,
    borderColor: '#edeaea'
  },
  resultsContainer: {
    borderTopWidth: 1,
    marginTop: 10,
    paddingTop: shotgun.contentPadding,
    borderColor: shotgun.silver
  },
  addressText: {
    fontSize: 16
  },
  smallText: {
    fontSize: 12,
    color: shotgun.brandLight,
  }
};

const getOrderedAddresses = (addreses) => {
  if (!addreses) {
    return addreses;
  }
  let result = [...addreses];
  result = result.filter(ad => !ad.isDefault);
  result.sort((e1, e2) => moment(e1.created).isBefore(moment(e2.created)));
  result = result.slice(0, MAX_RECENT_ADDRESSES);
  return result;
};

const getHomeAddress = (addreses) => {
  if (!addreses) {
    return addreses;
  }
  return addreses.find(ad => ad.isDefault) || addreses[0];
};

const getTabs = ({deliveryAddresses = [], suggestedPlaces = [], myLocation, hasLookedUpAddresses}) => {
  const result = [];
  if (suggestedPlaces.length || hasLookedUpAddresses){
    result.push('Suggested');
  }
  if (deliveryAddresses.length){
    result.push('Recent');
  }
  if (myLocation){
    result.push('Nearby Places');
  }
  return result;
};

const mapStateToProps = (state, initialProps) => {
  const { history, suggestedPlaces = [], hasLookedUpAddresses, isInBackground} = initialProps;

  if (isInBackground){
    return;
  }
  const me = getDaoState(state, ['user'], 'userDao');
  const deliveryAddresses = getDaoState(state, ['customer', 'deliveryAddresses'], 'deliveryAddressDao');
  const homeAddress = getHomeAddress(deliveryAddresses);
  
  const tabs = getTabs({deliveryAddresses, suggestedPlaces, hasLookedUpAddresses, myLocation: me});
  let selectedTabIndex = tabs.findIndex(tb => history.location.pathname.endsWith(tb));
  selectedTabIndex = !!~selectedTabIndex ? selectedTabIndex : 0;
  return {
    homeAddress,
    selectedTabIndex,
    selectedTab: tabs[selectedTabIndex],
    tabs,
    ...initialProps,
    me,
    ...getNavigationProps(initialProps),
    deliveryAddresses
  };
};


export default withExternalState(mapStateToProps)(AddressLookup);
