import React, {Component} from 'react';
import Payment from './Payment';
import DeliveryMap from './DeliveryMap';
import Delivery from './Delivery';
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

  componentWillMount(){
    this.setState(INITIAL_STATE);
  }

  render() {
    return <Switch>
      <Route path={'/Customer/Checkout/DeliveryMap'} exact render={() => <DeliveryMap {...this.props} context={this}/>} />
      <Route path={'/Customer/Checkout/Payment'} exact render={() => <Payment {...this.props} context={this}/>} />
      <Route path={'/Customer/Checkout/Delivery'} exact render={() => <Delivery {...this.props} context={this}/>} />
      <Route path={'/Customer/Checkout/DeliveryOptions'} exact render={() => <DeliveryOptions {...this.props} context={this}/>} />
      <Route path={'/Customer/Checkout/OrderConfirmation'} exact render={() => <OrderConfirmation {...this.props} context={this}/>} />
      <Route path={'/Customer/Checkout/OrderComplete'} exact render={() => <OrderComplete {...this.props} context={this}/>} />
      <Redirect to={'/Customer/Checkout/DeliveryMap'}/>
    </Switch>;
  }
}
