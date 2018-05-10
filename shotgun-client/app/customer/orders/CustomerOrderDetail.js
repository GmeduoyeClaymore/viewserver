import React, {Component} from 'react';
import {connect, ReduxRouter, Route} from 'custom-redux';
import {Container, Header, Left, Button, Body, Title, Content, Text, Tab, View, Spinner, Row} from 'native-base';
import {Icon, LoadingScreen, ErrorRegion, OrderSummary, Tabs, RatingSummary} from 'common/components';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getDao, getAnyOperationError} from 'common/dao';
import * as ContentTypes from 'common/constants/ContentTypes';
import OrderLifecycleView from 'common/components/orders/OrderLifecycleView';
import shotgun from 'native-base-theme/variables/shotgun';
import CustomerNegotiationPanel from './placed/CustomerNegotiationPanel';
import CustomerPriceSummary from './CustomerPriceSummary';
import CustomerStagedPaymentPanel from './progress/CustomerStagedPaymentPanel';
import CustomerHireOrderInProgress from './progress/CustomerHireOrderInProgress';
import CustomerJourneyOrderInProgress from './progress/CustomerJourneyOrderInProgress';
import CompleteControl from './progress/CompleteControl';
import CancelControl from './progress/CancelControl';
import VehicleDetails from './progress/VehicleDetails';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';

const JourneyJobInProgress = (message) => ({order}) => ( order.journeyOrderStatus == 'ENROUTE' ? <Row  style={{paddingLeft: 25, paddingTop: 10, paddingBottom: 25}}><Spinner style={{height: 15, marginRight: 10}}/><Text>{message}</Text></Row> : null);
class CustomerOrderDetail extends Component{
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
    this.state = {
      amount: undefined
    };
  }

  beforeNavigateTo(){
    this.subscribeToOrderSummary(this.props);
  }

  subscribeToOrderSummary(props){
    const {dispatch, orderId, order} = props;
    if (!order) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        ...OrderSummaryDao.CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS
      }));
    }
  }

  render() {
    const {busy, order, orderId, errors, history,dispatch} = this.props;
    const {resources} = this;
    const {InProgressControls, AcceptedControls} = resources;
    return busy || !order ? <LoadingScreen text={ !busy && !order ? 'Order "' + orderId + '" cannot be found' : 'Loading Order...'}/> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{resources.PageTitle(order)}</Title></Body>
      </Header>
      <Content>
        <View style={{paddingLeft: 15, paddingRight: 15}}>
          <ErrorRegion errors={errors}/>
          <OrderLifecycleView  orderStatus={order.orderStatus} price={order.amount} dispatch={dispatch} isRatingCustomer={false} userCreatedThisOrder={true} {...this.props}
            PlacedControls={[CustomerNegotiationPanel, OrderSummary]}
            InProgressControls={InProgressControls}
            AcceptedControls={AcceptedControls}
            CompletedControls={[CustomerPriceSummary, RatingSummary, OrderSummary]}
            CancelledControls={[CustomerPriceSummary, RatingSummary, OrderSummary]}
          />
        </View>
      </Content>
    </Container>;
  }
}

const findOrderSummaryFromDao = (state, orderId, daoName) => {
  const orderSummaries = getDaoState(state, ['orders'], daoName) || [];
  return  orderSummaries.find(o => o.orderId == orderId);
};

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  if (orderId === null){
    throw new Error('Must specify an order id to navigate to this page');
  }
  const daoState = getDao(state, 'singleOrderSummaryDao');
  if (!daoState){
    return null;
  }
  const order = findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');
  const {partnerResponses} = order || {};
  
  const errors = getAnyOperationError(state, 'orderDao');
  const isPendingOrderSummarySubscription = isAnyOperationPending(state, [{ singleOrderSummaryDao: 'resetSubscription'}]);
  return {
    ...initialProps,
    order,
    orderId,
    partnerResponses,
    isPendingOrderSummarySubscription,
    me: getDaoState(state, ['user'], 'userDao'),
    errors,
    busyUpdating: isAnyOperationPending(state, [{orderDao: 'cancelOrder'}, {orderDao: 'rejectResponse'}, {orderDao: 'updateOrderAmount'}]),
    busy: isPendingOrderSummarySubscription,
  };
};

const PaymentStagesAndSummary = (props) => {
  const {history, path, orderId, height, order} = props;
  const shouldShowPaymentStagesTab = order.paymentType !== 'DAYRATE' || (order.paymentStages && order.paymentStages.length);
  const paymentTabHeading = order.paymentType !== 'DAYRATE' ? 'Payment Stages' : 'Days Worked';
  const goToTabNamed = (name) => {
    history.replace({pathname: `${path}/${name}`, state: {orderId}});
  };
  return [<Tabs key="1" initialPage={history.location.pathname.endsWith('PaymentStages')  ? 1 : 0} page={history.location.pathname.endsWith('PaymentStages')  ? 1 : 0}  {...shotgun.tabsStyle}>
    <Tab heading='Summary' onPress={() => goToTabNamed('Summary')}/>
    {shouldShowPaymentStagesTab ? <Tab heading={paymentTabHeading} onPress={() => goToTabNamed('PaymentStages')}/> : null}
  </Tabs>,
  <ReduxRouter key="2"  name="CustomerOrdersRouter" {...props}  height={height - shotgun.tabHeight} path={path} defaultRoute='Summary'>
    <Route path={'Summary'} component={OrderSummary} />
    <Route path={'PaymentStages'} component={CustomerStagedPaymentPanel} />
  </ReduxRouter>];
};

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', () => 'Order Summary').
    delivery((order) => `${order.orderProduct.name} Delivery`).
    hire((order) => `${order.orderProduct.name}  Hire`).
    personell((order) => `${order.orderProduct.name} Job`).
    rubbish((order) => `${order.orderProduct.name} Rubbish Collection`).
  property('AcceptedControls', [OrderSummary]).
    personell([CustomerNegotiationPanel, CancelControl, PaymentStagesAndSummary/*, PersonellCustomerOrderInProgress*/]).
    hire([CustomerNegotiationPanel,CancelControl, OrderSummary]).
    delivery([CustomerNegotiationPanel, JourneyJobInProgress('Delivery In Progress'),CancelControl,VehicleDetails, props => <OrderSummary hideMap={true} {...props}/>]).
    rubbish([CustomerNegotiationPanel, JourneyJobInProgress('Collection In Progress'), ,CancelControl, VehicleDetails, props => <OrderSummary hideMap={true} {...props}/>]).
  property('InProgressControls', [OrderSummary]).
    personell([CustomerPriceSummary, CompleteControl, CancelControl, PaymentStagesAndSummary/*, PersonellCustomerOrderInProgress*/]).
    hire([CustomerPriceSummary,CompleteControl,CancelControl, CustomerHireOrderInProgress, OrderSummary]).
    delivery([CustomerPriceSummary, JourneyJobInProgress('Delivery In Progress'), CompleteControl,CancelControl, CustomerJourneyOrderInProgress,VehicleDetails, props => <OrderSummary hideMap={true} {...props}/>]).
    rubbish([CustomerPriceSummary, JourneyJobInProgress('Collection In Progress'),CompleteControl ,CancelControl, CustomerJourneyOrderInProgress, VehicleDetails, props => <OrderSummary hideMap={true} {...props}/>])
/*eslint-enable */

export default connect(
  mapStateToProps
)(CustomerOrderDetail);

