import React, {Component} from 'react';
import {connect, ReduxRouter, Route} from 'custom-redux';
import {Container, Header, Left, Button, Body, Title, Content, Text, Tab, View, Spinner} from 'native-base';
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
import CustomerCompleteControl from './progress/CustomerCompleteControl';
import CancelControl from './progress/CancelControl';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import OrderProgressPictures from 'common/components/orders/OrderProgressPictures';

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

  componentWillReceiveProps(newProps){
    if (newProps.orderId != this.props.orderId){
      this.subscribeToOrderSummary(newProps);
    }
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
    const {busy, order, orderId, errors, history, dispatch} = this.props;
    const {resources} = this;
    const {InProgressControls, AcceptedControls} = resources;
    return busy || !order ? <LoadingScreen text={ !busy && !order ? 'Order "' + orderId + '" cannot be found' : 'Loading Order...'}/> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{order.title}</Title></Body>
      </Header>
      <Content>
        <ErrorRegion errors={errors}/>
        <OrderLifecycleView  orderStatus={order.orderStatus} price={order.amount} dispatch={dispatch} isRatingCustomer={false} userCreatedThisOrder={true} {...this.props}
          PlacedControls={[CustomerNegotiationPanel, CancelControl, OrderSummary]}
          InProgressControls={InProgressControls}
          AcceptedControls={AcceptedControls}
          CompletedControls={[CustomerPriceSummary, RatingSummary, OrderSummary]}
          CancelledControls={[CustomerPriceSummary, RatingSummary, OrderSummary]}
        />
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
    busyUpdating: isAnyOperationPending(state, [{orderDao: 'cancelOrder'}, {orderDao: 'rejectResponse'}, {orderDao: 'updateOrderAmount'}, {orderDao: 'updateOrderVisibility'}]),
    busy: isPendingOrderSummarySubscription,
  };
};

const PaymentStagesAndSummary = (props) => {
  const {history, path, orderId, width, order} = props;

  const TabHeadings = [
    'Summary',
  ];

  if (order.orderStatus != 'ACCEPTED'){
    TabHeadings.push('Photos');
  }
  if (order.paymentType !== 'DAYRATE' || (order.paymentStages && order.paymentStages.length)){
    TabHeadings.push('PaymentStages');
  }
  const getSelectedTabIndex = (history, path) => {
    if (!history || !history.location || !history.location.pathname){
      return 0;
    }
    const result = TabHeadings.findIndex(th => history.location.pathname.includes(`${path}/${th}`));
    return !!~result ? result : 0;
  };
  const getHeading = (heading) => {
    if (heading === 'PaymentStages'){
      return order.paymentType !== 'DAYRATE' ? 'Payment Stages' : 'Days Worked';
    }
    return heading;
  };

  const selecedTabIndex = getSelectedTabIndex(history, path);

  const {images} = order;

  const goToTabNamed = (name) => {
    history.replace({pathname: `${path}/${name}`, state: {orderId}});
  };
  return [<Tabs key="1" initialPage={selecedTabIndex} page={selecedTabIndex}  {...shotgun.tabsStyle}>
    {TabHeadings.map(th =>  <Tab heading={getHeading(th)} onPress={() => goToTabNamed(th)} />)}
  </Tabs>,
  <ReduxRouter key="2" images={images} name="CustomerOrdersRouter" {...props} height={1000 /*hack to get around a weird height issue when keyboard shown*/} width={width} path={path} defaultRoute='Summary' hasFooter={true}>
    <Route path={'Summary'} component={OrderSummary} />
    <Route path={'PaymentStages'} component={CustomerStagedPaymentPanel} />
    <Route path={'Photos'} component={OrderProgressPictures} />
  </ReduxRouter>];
};

//TODO - bring this out into it's own file
const JourneyJobInProgress = (message) => ({order}) => ( order.journeyOrderStatus == 'ENROUTE' ? <View padded style={{flexDirection: 'row', marginBottom: 15, justifyContent: 'center'}}>
  <Spinner size={shotgun.isAndroid ? 30 : 1} color={shotgun.brandSuccess} style={styles.waitingSpinner}/>
  <Text style={{alignSelf: 'center'}} numberOfLines={1}>{message}</Text></View> : null);

const styles = {
  waitingSpinner: {
    height: 15,
    marginRight: 10,
    alignSelf: 'center'
  }
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
    delivery([CustomerNegotiationPanel, JourneyJobInProgress('Delivery In Progress'), CancelControl, props => <OrderSummary hideMap={true} {...props}/>]).
    rubbish([CustomerNegotiationPanel, JourneyJobInProgress('Collection In Progress'), CancelControl, props => <OrderSummary hideMap={true} {...props}/>]).
  property('InProgressControls', [OrderSummary]).
    personell([CustomerPriceSummary, CustomerCompleteControl, PaymentStagesAndSummary/*, PersonellCustomerOrderInProgress*/]).
    hire([CustomerPriceSummary,CustomerCompleteControl, CustomerHireOrderInProgress, OrderSummary]).
    delivery([CustomerPriceSummary, JourneyJobInProgress('Delivery In Progress'), CustomerCompleteControl, CustomerJourneyOrderInProgress, props => <OrderSummary hideMap={true} {...props}/>]).
    rubbish([CustomerPriceSummary, JourneyJobInProgress('Collection In Progress'), CustomerCompleteControl , CustomerJourneyOrderInProgress, props => <OrderSummary hideMap={true} {...props}/>])
/*eslint-enable */

export default connect(
  mapStateToProps
)(CustomerOrderDetail);

