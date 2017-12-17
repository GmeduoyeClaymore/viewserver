import React, {Component} from 'react';
import DeliveryMap from './DeliveryMap';
import ProductSelect from './ProductSelect';
import DeliveryOptions from './DeliveryOptions';
import OrderConfirmation from './OrderConfirmation';
import OrderComplete from './OrderComplete';
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './CheckoutInitialState';

export default class Checkout extends Component {
  constructor(){
    super();
    this.state = INITIAL_STATE;
  }

  render() {
    const controlProps = {...this.props};
    return <DeliveryMap {...controlProps} context={this}/>;
    /*return <Switch>
      <Route path={'/Customer/Checkout/ProductSelect'} exact render={() => <ProductSelect {...controlProps} context={this}/>} />
      <Route path={'/Customer/Checkout/DeliveryMap'} exact render={() => <DeliveryMap {...controlProps} context={this}/>} />
      <Route path={'/Customer/Checkout/DeliveryOptions'} exact render={() => <DeliveryOptions {...controlProps} context={this}/>} />
      <Route path={'/Customer/Checkout/OrderConfirmation'} exact render={() => <OrderConfirmation {...controlProps} context={this}/>} />
      <Route path={'/Customer/Checkout/OrderComplete'} exact render={() => <OrderComplete {...controlProps} context={this}/>} />
      <Redirect to={'/Customer/Checkout/DeliveryMap'}/>
    </Switch>;*/
  }
}
