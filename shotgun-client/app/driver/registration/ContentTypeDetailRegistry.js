import DriverCapabilityDetails from './DriverCapabilityDetails';
import ProductCategorySelector from './ProductCategorySelector';
import ProductSelector from './ProductSelector';
import * as ContentTypes from 'common/constants/ContentTypes';
import React from 'react';

const detailControlRegistry = {
  [ContentTypes.DELIVERY]: DriverCapabilityDetails,
  [ContentTypes.RUBBISH]: ProductCategorySelector,
  [ContentTypes.HIRE]: ProductCategorySelector,
  [ContentTypes.SKIP]: ProductCategorySelector,
  [ContentTypes.PERSONELL]: ProductSelector
};

export const resolveDetailsControl = (contentType) => {
  return detailControlRegistry[contentType.contentTypeId];
};

