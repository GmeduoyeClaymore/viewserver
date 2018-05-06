import React, {Component} from 'react';
import {connect, ReduxRouter, Route} from 'custom-redux';
import {Container, Header, Left, Button, Body, Title, Content, Text, Tab, View} from 'native-base';
import {Icon, LoadingScreen, ErrorRegion, SpinnerButton, OrderSummary, Tabs, RatingSummary} from 'common/components';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors} from 'common/dao';
import * as ContentTypes from 'common/constants/ContentTypes';
import OrderLifecycleView from 'common/components/orders/OrderLifecycleView';
import {cancelOrder} from 'customer/actions/CustomerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import CustomerNegotiationPanel from './placed/CustomerNegotiationPanel';
import DeliveryAndRubbishCustomerOrderInProgress from './progress/DeliveryAndRubbishCustomerOrderInProgress';
import PersonellCustomerOrderInProgress from './progress/PersonellCustomerOrderInProgress';
import CustomerStagedPaymentPanel from './progress/CustomerStagedPaymentPanel';
import CustomerHireOrderInProgress from './progress/CustomerHireOrderInProgress';
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
    const {dispatch, orderId, order, isPendingOrderSummarySubscription} = props;
    if (order == undefined && !isPendingOrderSummarySubscription) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        reportId: 'customerOrderSummary'
      }));
    }
  }

  render() {
    const {busy, order, orderId, errors, history, busyUpdating, dispatch, height, path} = this.props;
    const {resources} = this;
    const {InProgressControls} = resources;
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
          <CancelOrder dispatch={dispatch} orderId={order.orderId} busyUpdating={busyUpdating} />
          <OrderLifecycleView {...this.props}
            PlacedControls={[CustomerNegotiationPanel, OrderSummary]}
            InProgressControls={InProgressControls}
            AcceptedControls={InProgressControls}
            CompleteControls={[RatingSummary, OrderSummary]}
            CancelledControls={[RatingSummary, OrderSummary]}
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
  const order = findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');
  const {partnerResponses} = order || {};
  
  const errors = getOperationErrors(state, [{orderDao: 'cancelOrder'}, {orderDao: 'rejectResponse'}, {orderDao: 'updateOrderAmount'}, {orderDao: 'addPaymentStage'}, {orderDao: 'removePaymentStage'}, {orderDao: 'payForPaymentStage'}]);
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

const  CancelOrder = ({orderId, busyUpdating, dispatch, ...rest}) => {
  const onCancelOrder = () => {
    dispatch(cancelOrder({orderId}));
  };
  return <SpinnerButton  {...rest} disabledStyle={{opacity: 0.1}}  busy={busyUpdating} fullWidth danger onPress={onCancelOrder}><Text uppercase={false}>Cancel</Text></SpinnerButton>;
};

const PaymentStagesAndSummary = (props) => {
  const goToTabNamed = (name) => {
    const {history, path, orderId} = props;
    history.replace({pathname: `${path}/${name}`, state: {orderId}});
  };
  return [<Tabs key="1" initialPage={history.location.pathname.endsWith('PaymentStages')  ? 1 : 0} page={history.location.pathname.endsWith('PaymentStages')  ? 1 : 0}  {...shotgun.tabsStyle}>
    <Tab heading='Summary' onPress={() => goToTabNamed('Summary')}/>
    <Tab heading='PaymentStages' onPress={() => goToTabNamed('PaymentStages')}/>
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
  property('InProgressControls', [OrderSummary]).
    personell([PaymentStagesAndSummary, PersonellCustomerOrderInProgress]).
    hire([CustomerHireOrderInProgress, OrderSummary]).
    delivery([DeliveryAndRubbishCustomerOrderInProgress, OrderSummary]).
    rubbish([DeliveryAndRubbishCustomerOrderInProgress, OrderSummary])
/*eslint-enable */

export default connect(
  mapStateToProps
)(CustomerOrderDetail);

