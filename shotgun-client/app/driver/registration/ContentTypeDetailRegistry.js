import DriverCapabilityDetails from './DriverCapabilityDetails';
import ProductCategorySelector from './ProductCategorySelector';
import ProductSelector from './ProductSelector';
import * as ContentTypes from 'common/constants/ContentTypes';
import React from 'react';

const detailControlRegistry = {
  [ContentTypes.DELIVERY]: DriverCapabilityDetails,
  [ContentTypes.RUBBISH]: DriverCapabilityDetails,
  [ContentTypes.HIRE]: ProductCategorySelector,
  [ContentTypes.SKIP]: ProductCategorySelector
};

export const resolveDetailsControl = (contentType) => {
  const control = detailControlRegistry[contentType.contentTypeId];
  return control ? control : ProductSelector;
};

export default resolveDetailsControl;

