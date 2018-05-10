import React from 'react';
import {View, Text} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';

export default AddressMarker = ({address, color}) => {
  const markerColor = color ? color : shotgun.brandSecondary;

  return [
    <View key='bubble' mapBubble style={{borderColor: markerColor}}>
      <Text numberOfLines={1}>{address}</Text>
    </View>,
    <View key='arrow' mapArrow style={{borderTopColor: markerColor}}/>];
};
