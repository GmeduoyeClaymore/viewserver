import React from 'react';
import {Text, ListItem, View} from 'native-base';
import {addressToText} from 'common/components/maps/MapUtils';

export default Address = ({address = {}, onAddressSelected}) => <ListItem paddedTopBottom onPress={() => onAddressSelected(address)}>
  <View>
    <Text>{addressToText(address)}</Text>
    <Text smallText>{address.city}</Text>
  </View>
</ListItem>;
