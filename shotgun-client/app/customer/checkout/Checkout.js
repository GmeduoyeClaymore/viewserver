import React, {Component} from 'react';
import DeliveryMap from './DeliveryMap';
import ProductSelect from './ProductSelect';
import DeliveryOptions from './DeliveryOptions';
import VehicleDetails from './VehicleDetails';
import ItemDetails from './ItemDetails';
import OrderConfirmation from './OrderConfirmation';
import AddressLookup from 'common/components/maps/AddressLookup';
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './CheckoutInitialState';

export default class Checkout extends Component {
  constructor(){
    super();
    this.state = INITIAL_STATE;
  }

  render() {
    return <Switch>
      <Route path={'/Customer/Checkout/ProductSelect'} exact render={() => <ProductSelect {...this.props} context={this}/>} />
      <Route path={'/Customer/Checkout/DeliveryMap'} exact render={() => <DeliveryMap {...this.props} context={this}/>} />
      <Route path={'/Customer/Checkout/DeliveryOptions'} exact render={() => <DeliveryOptions {...this.props} context={this}/>} />
      <Route path={'/Customer/Checkout/VehicleDetails'} exact render={() => <VehicleDetails {...this.props} context={this}/>} />
      <Route path={'/Customer/Checkout/ItemDetails'} exact render={() => <ItemDetails {...this.props} context={this}/>} />
      <Route path={'/Customer/Checkout/AddressLookup'} exact render={() => <AddressLookup {...this.props} context={this}/>} />
      <Route path={'/Customer/Checkout/OrderConfirmation'} exact render={() => <OrderConfirmation {...this.props} context={this}/>} />
      <Redirect to={'/Customer/Checkout/ProductSelect'}/>
    </Switch>;
  }
}
