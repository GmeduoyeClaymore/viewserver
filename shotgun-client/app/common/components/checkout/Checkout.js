import React, {Component} from 'react';
import DeliveryMap from './DeliveryMap';
import ContentTypeSelect from './ContentTypeSelect';
import DeliveryOptions from './DeliveryOptions';
import VehicleSelect from './VehicleSelect';
import ItemDetails from './ItemDetails';
import OrderConfirmation from './OrderConfirmation';
import ProductCategoryList from './ProductCategoryList';
import FlatProductCategoryList from './FlatProductCategoryList';
import UsersForProductMap from './UsersForProductMap';
import ProductList from './ProductList';
import AddressLookup from 'common/components/maps/AddressLookup';
import {Route, ReduxRouter, withExternalState} from 'custom-redux';
import {INITIAL_STATE} from './CheckoutInitialState';
import ContentTypeNavigationStrategy from './ContentTypeNavigationStrategy';
import AddPropsToRoute from 'common/AddPropsToRoute';
class Checkout extends Component {
  static InitialState = INITIAL_STATE;
  static stateKey = 'customerCheckout';
  constructor(props){
    super(props);
    const {history, match} = props;
    this.navigationStrategy = new ContentTypeNavigationStrategy(history, match.path);
  }

  render() {
    const {navigationStrategy} = this;
    const {resetComponentState: resetParentComponentState} = this.props;
    const customerProps = {navigationStrategy, ...this.props, stateKey: Checkout.stateKey, resetParentComponentState};
    const {route: match = {}, height, width} = this.props;
    return <ReduxRouter height={height} width={width} defaultRoute={`${match.path}/ContentTypeSelect`}>
      <Route path={`${match.path}/ContentTypeSelect`} exact component={AddPropsToRoute(ContentTypeSelect, customerProps)} />
      <Route path={`${match.path}/DeliveryMap`} exact component={AddPropsToRoute(DeliveryMap, customerProps)} />
      <Route path={`${match.path}/DeliveryOptions`} exact component={AddPropsToRoute(DeliveryOptions, customerProps)} />
      <Route path={`${match.path}/VehicleSelect`} exact component={AddPropsToRoute(VehicleSelect, customerProps)} />
      <Route path={`${match.path}/ProductCategoryList`} exact component={AddPropsToRoute(ProductCategoryList, customerProps)} />
      <Route path={`${match.path}/FlatProductCategoryList`} exact component={AddPropsToRoute(FlatProductCategoryList, customerProps)} />
      <Route path={`${match.path}/ProductList`} exact component={AddPropsToRoute(ProductList, customerProps)} />
      <Route path={`${match.path}/ItemDetails`} exact component={AddPropsToRoute(ItemDetails, customerProps)} />
      <Route path={`${match.path}/AddressLookup`} exact component={AddPropsToRoute(AddressLookup, customerProps)} />
      <Route path={`${match.path}/OrderConfirmation`} exact component={AddPropsToRoute(OrderConfirmation, customerProps)} />
      <Route path={`${match.path}/UsersForProductMap`} exact component={AddPropsToRoute(UsersForProductMap, customerProps)} />
    </ReduxRouter>;
  }
}

export default withExternalState()(Checkout);
