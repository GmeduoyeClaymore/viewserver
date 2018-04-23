import React, {Component} from 'react';
import {Text, List, View} from 'native-base';
import {LoadingScreen} from 'common/components';
import Address from './Address';

export default class ReverseGeoAddresses extends Component {
  constructor(props) {
    super(props);
    this.state = {
      hasLookedUp: false,
      reverseLookedUpAddresses: [],
      busy: false
    };
  }

  beforeNavigateTo() {
    this.reverseGeoCodeSearch();
  }

  reverseGeoCodeSearch = async() => {
    const {client, myLocation} = this.props;
    const {latitude, longitude} = myLocation;
    try {
      super.setState({
        hasLookedUp: false,
        reverseLookedUpAddresses: [],
        busy: true
      });
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
      super.setState({error});
    }
  }

  render() {
    const {onAddressSelected} = this.props;
    const {reverseLookedUpAddresses = [], busy} = this.state;
    if (busy) {
      return <LoadingScreen text={'Looking for places close to you...'}/>;
    }
    return reverseLookedUpAddresses.length > 0 ? <View paddedLeft style={{paddingTop: 15}}>
      <Text smallText>Places close to current location</Text>
      <List>
        {reverseLookedUpAddresses.map((ad, idx) => <Address address={ad} key={idx} onAddressSelected={onAddressSelected}/>)}
      </List>
    </View> : null;
  }
}
