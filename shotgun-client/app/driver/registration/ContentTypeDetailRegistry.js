import DriverCapabilityDetails from './DriverCapabilityDetails';
import * as ContentTypes from 'common/constants/ContentTypes';
import {View} from 'react-native';
import {Text} from 'native-base';
import React from 'react';

const detailControlRegistry = {
  [ContentTypes.DELIVERY]: DriverCapabilityDetails
};

export const resolveDetailsControl = (contentType) => {
  const control = detailControlRegistry[contentType.contentTypeId];
  return control ? control : () => <View><Text>{contentType.name + ' details'}</Text></View>;
};

export default resolveDetailsControl;

