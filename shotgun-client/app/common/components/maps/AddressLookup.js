import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withExternalState, setStateIfIsMounted } from 'custom-redux';
import moment from 'moment';
import { Text, Button, Header, Left, Body, Container, Title, Input, Grid, Row, List, ListItem, View, Content} from 'native-base';
import { getDaoState, getNavigationProps } from 'common/dao';
import { parseGooglePlacesData } from 'common/components/maps/MapUtils';
import {ErrorRegion, Icon} from 'common/components';
import {debounce} from 'lodash';
import shotgun from 'native-base-theme/variables/shotgun';
import {addressToText} from 'common/utils';

const MAX_RECENT_ADDRESSES = 10;

const SuggestedPlace = ({result, onSuggestedPlaceSelected}) => {
  return <ListItem paddedTopBottom onPress={() => onSuggestedPlaceSelected(result)}>
    <View>
      <Text style={styles.addressText}>{result.structured_formatting.main_text}</Text>
      <Text style={styles.smallText}>{result.structured_formatting.secondary_text}</Text>
    </View>
  </ListItem>;
};

const Address = ({address = {}, onAddressSelected}) => <ListItem paddedTopBottom onPress={() => onAddressSelected(address)}>
  <View>
    <Text style={styles.addressText}>{addressToText(address)}</Text>
    <Text style={styles.smallText}>{address.city}</Text>
  </View>
</ListItem>;

const HomeAddressItem = ({address = {}, onAddressSelected}) => <ListItem paddedTopBottom first onPress={() => onAddressSelected(address)}>
  <View>
    <Text style={styles.addressText}>Home</Text>
    <Text style={styles.smallText}>{`${address.line1}, ${address.postCode}`}</Text>
  </View>
</ListItem>;

const CurrentLocation = ({onCurrentLocation}) => <ListItem paddedTopBottom first onPress={() => onCurrentLocation()}>
  <Row style={{marginLeft: 25}} size={10}>
    <Icon name="pin" paddedIcon originPin style={{ alignSelf: 'center' }} />
    <Text style={styles.addressText}>Current Location</Text>
  </Row>
</ListItem>;


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

class AddressLookup extends Component {
  static propTypes = {
    client: PropTypes.object,
    addressLabel: PropTypes.string.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      busy: false,
      addressSearchText: undefined,
      suggestedPlaces: [],
      errors: undefined
    };
    this.searchAutoCompleteSuggestions = debounce(this.searchAutoCompleteSuggestions.bind(this), 50);
    this.onSuggestedPlaceSelected = this.onSuggestedPlaceSelected.bind(this);
    this.onAddressChanged = this.onAddressChanged.bind(this);
    this.reverseGeoCodeSearch = this.reverseGeoCodeSearch.bind(this);
    this.onAddressSelected = this.onAddressSelected.bind(this);
  }

  async searchAutoCompleteSuggestions(value){
    const {client, me = {}} = this.props;
    try {
      super.setState({ busy: true });
      const responseJSON = await client.invokeJSONCommand('mapsController', 'makeAutoCompleteRequest', {
        lat: me.latitude,
        lng: me.longitude,
        input: value,
        language: 'en'
      });

      const filteredPredictions = responseJSON.predictions;

      super.setState({
        suggestedPlaces: filteredPredictions,
        reverseLookedUpAddresses: [],
        busy: false
      });
    } catch (error) {
      super.setState({ error });
    }
  }

  async reverseGeoCodeSearch(){
    const {client, me} = this.props;
    const {latitude, longitude} = me;
    try {
      super.setState({ busy: true });
      const reverseLookedUpAddresses = await client.invokeJSONCommand('mapsController', 'getAddressesFromLatLong', {
        latitude, longitude
      });

      super.setState({
        reverseLookedUpAddresses,
        suggestedPlaces: undefined,
        busy: false
      });
    } catch (error) {
      super.setState({ error });
    }
  }

  onAddressChanged(value){
    super.setState({ addressSearchText: value }, () => this.searchAutoCompleteSuggestions(value));
  }

  onAddressSelected(value){
    const {addressPath, setStateWithPath, history, dispatch} = this.props;
    if (!addressPath){
      throw new Error('This control needs an address path');
    }
    setStateWithPath(value, addressPath, history.goBack, dispatch);
  }

  async onSuggestedPlaceSelected(rowData){
    try {
      const {client} = this.props;
      const {onAddressSelected} = this;
      const res = await client.invokeJSONCommand('mapsController', 'mapPlaceRequest', {
        placeid: rowData.place_id,
        language: 'en'
      }).timeoutWithError(5000, 'Place request timed out');

      onAddressSelected(parseGooglePlacesData(res.result));
    } catch (error) {
      super.setState({ error });
    }
  }

  render() {
    const { addressSearchText, suggestedPlaces = [], errors, reverseLookedUpAddresses = []} = this.state;
    const { deliveryAddresses = [], addressLabel, history, me} = this.props;
    const orderedAddresses = getOrderedAddresses(deliveryAddresses);
    const homeAddress = getHomeAddress(deliveryAddresses);
    const {onSuggestedPlaceSelected, onAddressChanged, onAddressSelected} = this;

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
          <Grid>
            <Row size={10} style={styles.searchContainer}>
              <ErrorRegion errors={errors}>
                <Icon name="pin" paddedIcon originPin style={{ alignSelf: 'center' }} />
                <Input placeholder={addressLabel} value={addressSearchText} autoFocus={true} onChangeText={onAddressChanged} />
              </ErrorRegion>
            </Row>
            {me ? <CurrentLocation onCurrentLocation={this.reverseGeoCodeSearch}/> : null}
            <Row size={80}>
              {deliveryAddresses && deliveryAddresses.length && suggestedPlaces.length == 0 && reverseLookedUpAddresses.length == 0 ? <Container paddedLeft style={styles.resultsContainer}>
                <Text style={styles.smallText}>Recent Addresses</Text>
                <List>
                  {homeAddress ? <HomeAddressItem address={homeAddress} onAddressSelected={onAddressSelected}/> : null}
                  {orderedAddresses.map((ad, idx) => <Address address={ad} key={idx} onAddressSelected={onAddressSelected}/>)}
                </List>
              </Container> : null}

              {suggestedPlaces.length > 0 ? <Container paddedLeft>
                <Text style={styles.smallText}>Results</Text>
                <List>{suggestedPlaces.map((r, i) => <SuggestedPlace key={i} result={r} onSuggestedPlaceSelected={onSuggestedPlaceSelected}/>)}</List>
              </Container> : null}

              {reverseLookedUpAddresses.length > 0 ? <Container paddedLeft style={{paddingTop: 15}}>
                <Text style={styles.smallText}>Places close to current location</Text>
                <List>
                  {reverseLookedUpAddresses.map((ad, idx) => <Address address={ad} key={idx} onAddressSelected={onAddressSelected}/>)}
                </List>
              </Container> : null}
            </Row>
          </Grid>
        </Content>
      </Container>
    );
  }
}

const styles = {
  title: {
    fontSize: 20,
  },
  searchContainer: {
    paddingLeft: shotgun.contentPadding
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

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  me: getDaoState(state, ['user'], 'userDao'),
  ...getNavigationProps(initialProps),
  ...getDaoState(state, ['customer'], 'deliveryAddressDao'),
});

export default withExternalState(mapStateToProps)(AddressLookup);
