import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Container, Header, Left, Button, Body, Title, Content} from 'native-base';
import {OrderSummary, Icon, LoadingScreen, RatingSummary, ErrorRegion} from 'common/components';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors} from 'common/dao';
import * as ContentTypes from 'common/constants/ContentTypes';
import OrderStatusButtons from './OrderStatusButtons';
import OrderPriceControl from './OrderPriceControl';
import DriverDetails from './DriverDetails';
import MapDetails from './MapDetails';

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', () => 'Order Summary').
    delivery(() => 'Delivery Job').
    personell(({product}) => `${product.name} Job`).
    rubbish(() => 'Rubbish Collection');
/*eslint-disable */

class CustomerOrderDetail extends Component{
  constructor(props) {
    super(props);
    this.onFixedPriceValueChanged = this.onFixedPriceValueChanged.bind(this);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  componentDidMount(){
    this.subscribeToOrderSummary(this.props);
  }

  componentWillReceiveProps(newProps){
    this.subscribeToOrderSummary(newProps);
  }

  subscribeToOrderSummary(props){
    const {dispatch, orderId, orderSummary} = props;
    if (orderSummary == undefined) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        reportId: 'customerOrderSummary'
      }));
    }
  }

  render() {
    const {resources} = this;
    return busy ? <LoadingScreen text="Loading Order"/> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{resources.PageTitle(orderSummary)}</Title></Body>
      </Header>
      <Content>
        <ErrorRegion errors={errors}/>
        <OrderStatusButtons {...this.props}/>
        <OrderPriceControl {...this.props}/>
        <MapDetails {...this.props}/>
        <DriverDetails  {...this.props}/>
        <RatingSummary  {...this.props}/>
        <OrderSummary   {...this.props}/>
      </Content>
    </Container>;
  }
}

const findOrderSummaryFromDao = (state, orderId, daoName) => {
  const orderSummaries = getDaoState(state, ['orders'], daoName) || [];
  return  orderSummaries.find(o => o.orderId == orderId);
}

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  let orderSummary = findOrderSummaryFromDao(state,orderId,'orderSummaryDao');
  orderSummary = orderSummary || findOrderSummaryFromDao(state,orderId,'singleOrderSummaryDao');
  
  const {contentType: selectedContentType} = (orderSummary || {});
  const errors = getOperationErrors(state, [{customerDao: 'cancelOrder'}, {customerDao: 'rejectDriver'}, {customerDao: 'updateOrderPrice'}])
  return {
    ...initialProps,
    selectedContentType,
    orderId,
    errors,
    busyUpdating: isAnyOperationPending(state, [{customerDao: 'cancelOrder'}, {customerDao: 'rejectDriver'}, {customerDao: 'updateOrderPrice'}]),
    busy: isAnyOperationPending(state, [{ singleOrderSummaryDao: 'resetSubscription'}]) || orderSummary == undefined,
    orderSummary
  };
};

export default connect(
  mapStateToProps
)(CustomerOrderDetail);

