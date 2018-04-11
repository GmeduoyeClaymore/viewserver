
import React, {Component} from 'react';
import {PriceSummary} from 'common/components';
import {updateOrderPrice} from 'customer/actions/CustomerActions';
import * as ContentTypes from 'common/constants/ContentTypes';

const staticPriceControl = (props) => <PriceSummary {...props}/>;
const dynamicPriceControl = ({price, orderSummary = {}, onValueChanged, ...props}) => {
  const {userId} = props;
  return hasStarted(orderSummary.status) || orderSummary.customerUserId != userId ?
    <PriceSummary price={orderSummary.totalPrice}  {...props}/> :
    <Col>
      <PriceSummary price={orderSummary.totalPrice} onValueChanged={onValueChanged} {...props}/>
      <CurrencyInput style={styles.input} {...props} placeholder="Enter Fixed Price" initialPrice={price} onValueChanged={onValueChanged}/>
    </Col>;
};
/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PricingControl', staticPriceControl).
    personell(dynamicPriceControl)
/*eslint-enable */

export default class OrderPriceControl extends Component{
  constructor(props){
    super(props);
    this.onFixedPriceValueChanged = this.onFixedPriceValueChanged.bind(this);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  onFixedPriceValueChanged(newPrice){
    const {orderSummary, dispatch} = this.props;
    dispatch(updateOrderPrice(orderSummary.orderId, newPrice));
  }

  render(){
    const {orderSummary = {status: ''}, busyUpdating, userId} = this.props;
    const {delivery = {}} = orderSummary;
    const {resources} = this;
    const {PricingControl} = resources;
    <PricingControl readonly={busyUpdating} userId={userId} onValueChanged={this.onFixedPriceValueChanged} isFixedPrice={delivery.isFixedPrice} orderStatus={orderSummary.status} isDriver={false} orderSummary={orderSummary} price={orderSummary.totalPrice}/>;
  }
}
