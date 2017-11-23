import React, {Component} from 'react';
import Payment from './Payment';
import Delivery from './Delivery';
import DeliveryOptions from './DeliveryOptions';
import OrderConfirmation from './OrderConfirmation';
import OrderComplete from './OrderComplete';
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './CheckoutInitialState';

export default class Checkout extends Component {
  constructor(){
    super();
  }

  componentWillMount(){
    this.state = INITIAL_STATE;
  }

  render() {
    const {match} = this.props;

    return <Switch>
      <Route path={`${match.path}/Checkout/Payment`} exact render={() => <Payment {...this.props} context={this}/>} />
      <Route path={`${match.path}/Checkout/Delivery`} exact render={() => <Delivery {...this.props} context={this}/>} />
      <Route path={`${match.path}/Checkout/DeliveryOptions`} exact render={() => <DeliveryOptions {...this.props} context={this}/>} />
      <Route path={`${match.path}/Checkout/OrderConfirmation`} exact render={() => <OrderConfirmation {...this.props} context={this}/>} />
      <Route path={`${match.path}/Checkout/OrderComplete`} exact render={() => <OrderComplete {...this.props} context={this}/>} />
      <Redirect to={`${match.path}/Checkout/Payment`}/>
    </Switch>;
  }
}
