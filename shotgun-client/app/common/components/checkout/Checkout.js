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
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './CheckoutInitialState';
import ContentTypeNavigationStrategy from './ContentTypeNavigationStrategy';
import { withRouter } from 'react-router';
import {withExternalState} from 'custom-redux';

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
    const {match = {}} = this.props;
    return <Switch>
      <Route path={`${match.path}/ContentTypeSelect`} exact render={() => <ContentTypeSelect {...customerProps}/>} />
      <Route path={`${match.path}/DeliveryMap`} exact render={() => <DeliveryMap {...customerProps}/>} />
      <Route path={`${match.path}/DeliveryOptions`} exact render={() => <DeliveryOptions {...customerProps}/>} />
      <Route path={`${match.path}/VehicleSelect`} exact render={() => <VehicleSelect {...customerProps}/>} />
      <Route path={`${match.path}/ProductCategoryList`} exact render={() => <ProductCategoryList {...customerProps}/>} />
      <Route path={`${match.path}/FlatProductCategoryList`} exact render={() => <FlatProductCategoryList {...customerProps}/>} />
      <Route path={`${match.path}/ProductList`} exact render={() => <ProductList {...customerProps}/>} />
      <Route path={`${match.path}/ProductDetails`} exact render={() => <ProductDetails {...customerProps}/>} />
      <Route path={`${match.path}/ItemDetails`} exact render={() => <ItemDetails {...customerProps}/>} />
      <Route path={`${match.path}/AddressLookup`} exact render={() => <AddressLookup {...customerProps}/>} />
      <Route path={`${match.path}/OrderConfirmation`} exact render={() => <OrderConfirmation {...customerProps}/>} />
      <Route path={`${match.path}/UsersForProductMap`} exact render={() => <UsersForProductMap {...customerProps}/>} />
      <Redirect to={`${match.path}/ContentTypeSelect`}/>
    </Switch>;
  }
}

export default withRouter(withExternalState()(Checkout));
