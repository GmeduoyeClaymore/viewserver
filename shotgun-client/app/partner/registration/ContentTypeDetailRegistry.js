import PartnerVehicleDetails from './PartnerVehicleDetails';
import ProductCategorySelector from './ProductCategorySelector';
import HeirarchicalProductCategorySelector from './HeirarchicalProductCategorySelector';
import ProductSelector from './ProductSelector';
import * as ContentTypes from 'common/constants/ContentTypes';
import React from 'react';

const detailControlRegistry = {
  [ContentTypes.DELIVERY]: PartnerVehicleDetails,
  [ContentTypes.RUBBISH]: ProductCategorySelector,
  [ContentTypes.HIRE]: HeirarchicalProductCategorySelector,
  [ContentTypes.SKIP]: HeirarchicalProductCategorySelector,
  [ContentTypes.PERSONELL]: ProductSelector
};

export const resolveDetailsControl = (contentType) => {
  return detailControlRegistry[contentType.contentTypeId];
};

