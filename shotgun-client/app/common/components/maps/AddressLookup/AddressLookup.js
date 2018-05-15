import React, {Component} from 'react';
import {withExternalState, ReduxRouter, Route} from 'custom-redux';
import {Button, Header, Left, Body, Container, Title, Input, Tab, Row, Content} from 'native-base';
import {getDaoState, getNavigationProps} from 'common/dao';
import {ErrorRegion, Icon, Tabs} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';
import Logger from 'common/Logger';
import SuggestedAddresses from './SuggestedAddresses';
import RecentAddresses from './RecentAddresses';
import ReverseGeoAddresses from './ReverseGeoAddresses';
import {Keyboard} from 'react-native';
import {debounce} from 'lodash';

class AddressLookup extends Component {
  constructor(props) {
    super(props);
    this.state = {
      busy: false,
      addressSearchText: undefined,
      suggestedPlaces: [],
      hasLookedUpAddresses: false,
      errors: undefined
    };
  }

  componentWillReceiveProps(newProps) {
    this.goToTabNamed(newProps.selectedTab, newProps);
  }

  onNavigateAway() {
    Keyboard.dismiss();
    this.setState({addressSearchText: undefined, suggestedPlaces: [],  hasLookedUpAddresses: false});
  }

  onAddressChanged = (value) => {
    this.goToTabNamed('Suggested');
    this.setState({addressSearchText: value}, () => this.searchAutoCompleteSuggestions(value));
  }

  goToTabNamed = (name, propOverride) => {
    const {history, path, addressLabel, addressPath} = propOverride || this.props;
    if (!history.location.pathname.endsWith(name)) {
      history.replace({pathname: `${path}/${name}`, state: {addressLabel, addressPath}});
    }
  }

  searchAutoCompleteSuggestions = debounce(async (value, continueWith) => {
    const {client, me = {}} = this.props;
    try {
      if (this.pendingAutoCompletePromise) {
        this.pendingAutoCompletePromise.cancel();
      }
      if (!value) {
        Logger.info(`Clearing results for empty search term ${value}`);
        this.setState({
          suggestedPlaces: undefined,
          hasLookedUpAddresses: false,
          busy: false
        });
        return;
      }
      this.setState({busy: true, hasLookedUpAddresses: true});
      this.pendingAutoCompletePromise = client.invokeJSONCommand('mapsController', 'makeAutoCompleteRequest', {
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
      this.setState({error});
    }
  }, 300)

  onAddressSelected = async (value) => {
    const {addressPath, setStateWithPath, history, dispatch} = this.props;
    if (!addressPath) {
      throw new Error('This control needs an address path');
    }
    setStateWithPath(value, addressPath, history.goBack, dispatch);
  }

  render() {
    const {deliveryAddresses = [], addressLabel, homeAddress, history, height, width, path, selectedTabIndex, client, tabs, me, addressSearchText, suggestedPlaces = [], errors, hasLookedUpAddresses, busy, addressPath} = this.props;
    if (!addressLabel){
      return null;
    }
    return (
      <Container>
        <Header withButton>
          <Left>
            <Button>
              <Icon name='cross' onPress={() => history.goBack()}/>
            </Button>
          </Left>
          <Body><Title style={styles.title}>{addressLabel}</Title></Body>
        </Header>
        <Content keyboardShouldPersistTaps="always">
          <Row size={10} style={styles.searchContainer}>
            <ErrorRegion errors={errors}/>
            <Icon name="pin" paddedIcon originPin style={{alignSelf: 'center'}}/>
            <Input placeholder={`Enter ${addressLabel.toLowerCase()}`} autoCorrect={false} returnKeyType={'done'} style={styles.input}
              value={addressSearchText} autoFocus={true} onChangeText={this.onAddressChanged}/>
          </Row>
          {tabs.length ? <Tabs initialPage={selectedTabIndex} page={selectedTabIndex} {...shotgun.tabsStyle} >
            {tabs.map(tab => <Tab key={tab} heading={tab} onPress={() => this.goToTabNamed(tab)}/>)}
          </Tabs> : null}
          {tabs.length ? <ReduxRouter style={styles.router} client={client} myLocation={me}
            onAddressSelected={this.onAddressSelected} path={path} name="AddressLookupRouter"
            height={height - shotgun.tabHeight} width={width}
            defaultRoute={{pathname: `${tabs[0]}`, state: {addressLabel, addressPath}}}>
            <Route path={'Recent'} component={RecentAddresses} homeAddress={homeAddress} deliveryAddresses={deliveryAddresses}/>
            <Route path={'Suggested'} component={SuggestedAddresses} {...{hasLookedUpAddresses, suggestedPlaces, addressSearchText, busy}}/>
            <Route path={'Nearby Places'} component={ReverseGeoAddresses}/>
          </ReduxRouter> : null}
        </Content>
      </Container>
    );
  }
}

const styles = {
  title: {
    fontSize: 20,
  },
  router: {
    paddingLeft: 10,
    paddingRight: 10
  },
  searchContainer: {
    backgroundColor: shotgun.brandPrimary,
    paddingLeft: shotgun.contentPadding,
  }
};


const getTabs = (showRecent) => {
  const result = [];

  if (showRecent){
    result.push('Recent');
  }
  result.push('Suggested');

  /* if (myLocation) {
    result.push('Nearby Places');
  }*/
  return result;
};

const mapStateToProps = (state, initialProps) => {
  const {history, isInBackground, showRecent = true} = initialProps;

  if (isInBackground) {
    return;
  }
  const me = getDaoState(state, ['user'], 'userDao');
  const deliveryAddresses = getDaoState(state, ['customer', 'deliveryAddresses'], 'deliveryAddressDao');
  const homeAddress = getDaoState(state, ['customer', 'homeAddress'], 'deliveryAddressDao');

  const tabs = getTabs(showRecent);
  let selectedTabIndex = tabs.findIndex(tb => history.location.pathname.endsWith(tb));
  selectedTabIndex = !!~selectedTabIndex ? selectedTabIndex : 0;
  return {
    homeAddress,
    selectedTabIndex,
    selectedTab: tabs[selectedTabIndex],
    tabs,
    me,
    deliveryAddresses,
    ...getNavigationProps(initialProps),
    ...initialProps
  };
};

export default withExternalState(mapStateToProps)(AddressLookup);
