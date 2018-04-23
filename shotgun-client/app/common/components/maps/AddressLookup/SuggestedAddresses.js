import React, {Component} from 'react';
import {Text, List, ListItem, View} from 'native-base';
import {parseGooglePlacesData} from 'common/components/maps/MapUtils';
import {ErrorRegion, LoadingScreen} from 'common/components';

export default class SuggestedAddresses extends Component {
  constructor(props) {
    super(props);
    this.state = {
      errors: undefined
    };
  }

  onSuggestedPlaceSelected = async(rowData) => {
    try {
      const {client, onAddressSelected} = this.props;
      const res = await client.invokeJSONCommand('mapsController', 'mapPlaceRequest', {
        placeid: rowData.place_id,
        language: 'en'
      }).timeoutWithError(5000, 'Place request timed out');

      await onAddressSelected(parseGooglePlacesData(res.result));
    } catch (error) {
      super.setState({error});
    }
  }

  render() {
    const {suggestedPlaces, hasLookedUpAddresses, busy, addressSearchText} = this.props;
    const {errors} = this.state;
    if (busy) {
      return <LoadingScreen text={`Looking up results for ${addressSearchText}...`}/>;
    }
    return hasLookedUpAddresses ? <View paddedLeft style={{paddingTop: 15}}><ErrorRegion errors={errors}/>
      <Text smallText>{suggestedPlaces.length ? `${suggestedPlaces.length > 1 ? 'Addresses' : 'Address'} matching "${addressSearchText}"` : `No Results Found matching "${addressSearchText}"`}</Text>
      <List>{suggestedPlaces.map((r, i) => <SuggestedPlace key={i} result={r} onSuggestedPlaceSelected={this.onSuggestedPlaceSelected}/>)}</List>
    </View> : null;
  }
}

const SuggestedPlace = ({result, onSuggestedPlaceSelected}) => {
  return <ListItem paddedTopBottom onPress={() => onSuggestedPlaceSelected(result)}>
    <View>
      <Text>{result.structured_formatting.main_text}</Text>
      <Text smallText>{result.structured_formatting.secondary_text}</Text>
    </View>
  </ListItem>;
};
