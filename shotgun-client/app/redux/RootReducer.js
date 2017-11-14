import {combineReducers} from 'redux';
import CheckoutReducer from '../customer/checkout/CheckoutReducer';
import CustomerReducer from '../customer/CustomerReducer';
import ProductReducer from '../customer/product/ProductReducer';

export default rootReducer = combineReducers({
  CheckoutReducer,
  CustomerReducer,
  ProductReducer
});
