import React, {Component} from 'react';
import DeliveryMap from './DeliveryMap';
import ContentTypeSelect from './ContentTypeSelect';
import DeliveryOptions from './DeliveryOptions';
import VehicleSelect from './VehicleSelect';
import ItemDetails from './ItemDetails';
import OrderConfirmation from './OrderConfirmation';
import ProductCategoryList from './ProductCategoryList';
import ProductList from './ProductList';
import AddressLookup from 'common/components/maps/AddressLookup';
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './CheckoutInitialState';
import ContentTypeNavigationStrategy from './ContentTypeNavigationStrategy';

export default class Checkout extends Component {
  constructor(props){
    super(props);
    this.state = INITIAL_STATE;
    this.navigationStrategy = new ContentTypeNavigationStrategy(props.history);
  }

  render() {
    const {navigationStrategy} = this;
    const customerProps = {navigationStrategy, ...this.props};
    return <Switch>
      <Route path={'/Customer/Checkout/ContentTypeSelect'} exact render={() => <ContentTypeSelect {...customerProps} context={this}/>} />
      <Route path={'/Customer/Checkout/DeliveryMap'} exact render={() => <DeliveryMap {...customerProps} context={this}/>} />
      <Route path={'/Customer/Checkout/DeliveryOptions'} exact render={() => <DeliveryOptions {...customerProps} context={this}/>} />
      <Route path={'/Customer/Checkout/VehicleSelect'} exact render={() => <VehicleSelect {...customerProps} context={this}/>} />
      <Route path={'/Customer/Checkout/ProductCategoryList'} exact render={() => <ProductCategoryList {...customerProps} context={this}/>} />
      <Route path={'/Customer/Checkout/ProductList'} exact render={() => <ProductList {...customerProps} context={this}/>} />
      <Route path={'/Customer/Checkout/ProductDetails'} exact render={() => <ProductDetails {...customerProps} context={this}/>} />
      <Route path={'/Customer/Checkout/ItemDetails'} exact render={() => <ItemDetails {...customerProps} context={this}/>} />
      <Route path={'/Customer/Checkout/AddressLookup'} exact render={() => <AddressLookup {...customerProps} context={this}/>} />
      <Route path={'/Customer/Checkout/OrderConfirmation'} exact render={() => <OrderConfirmation {...customerProps} context={this}/>} />
      <Redirect to={'/Customer/Checkout/VehicleSelect'}/>
    </Switch>;
  }
}
