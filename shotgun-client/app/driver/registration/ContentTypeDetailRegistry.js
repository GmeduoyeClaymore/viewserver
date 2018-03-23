import DriverCapabilityDetails from './DriverCapabilityDetails';
import ProductCategorySelector from './ProductCategorySelector';
import ProductSelector from './ProductSelector';
import * as ContentTypes from 'common/constants/ContentTypes';
import React from 'react';

const detailControlRegistry = {
  [ContentTypes.DELIVERY]: DriverCapabilityDetails,
  [ContentTypes.RUBBISH]: (props) => { return (<ProductCategorySelector {...props} hideSearchBar={true}/>);},
  [ContentTypes.HIRE]: ProductCategorySelector,
  [ContentTypes.SKIP]: ProductCategorySelector
};

const detailClassRegistry = {
  [ContentTypes.DELIVERY]: DriverCapabilityDetails,
  [ContentTypes.RUBBISH]: ProductCategorySelector,
  [ContentTypes.HIRE]: ProductCategorySelector,
  [ContentTypes.SKIP]: ProductCategorySelector
};

export const resolveDetailsControl = (contentType) => {
  const control = detailControlRegistry[contentType.contentTypeId];
  return control ? control : ProductSelector;
};

export const resolveDetailsClass = (contentType) => {
  const control = detailClassRegistry[contentType.contentTypeId];
  return control ? control : ProductSelector;
};

export default resolveDetailsControl;

