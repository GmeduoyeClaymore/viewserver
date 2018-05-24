import React, { Component } from 'react';
import { withExternalState, ReduxRouter, Route } from 'custom-redux';
import { resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors, findOrderSummaryFromDao, getAnyOperationError } from 'common/dao';
import { Container, Header, Left, Button, Body, Title, Content, Tab } from 'native-base';
import { OrderSummary, LoadingScreen, Icon, ErrorRegion, RatingSummary, Tabs } from 'common/components';
import PartnerOrderLifecycleView from 'common/components/orders/PartnerOrderLifecycleView';
import OrderProgressPictures from 'common/components/orders/OrderProgressPictures';
import { respondToOrder } from 'partner/actions/PartnerActions';
import * as ContentTypes from 'common/constants/ContentTypes';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import PartnerNegotiationPanel from './PartnerNegotiationPanel';
import PartnerStagedPaymentPanel from './PartnerStagedPaymentPanel';
import PartnerPriceSummary from './PartnerPriceSummary';
import shotgun from 'native-base-theme/variables/shotgun';
import DayRatePersonellOrderInProgress from './progress/DayRatePersonellOrderInProgress';
import FixedPersonellOrderInProgress from './progress/FixedPersonellOrderInProgress';
import PartnerJourneyOrderInProgress from './progress/PartnerJourneyOrderInProgress';
import HireOrderInProgress from './progress/HireOrderInProgress';
import PartnerResponseRejected from './PartnerResponseRejected';

class PartnerOrderDetail extends Component {
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  beforeNavigateTo() {
    const { dispatch, orderId, order, responseParams } = this.props;
    if (!order){
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        ...OrderSummaryDao.PARTNER_ORDER_SUMMARY_DEFAULT_OPTIONS
      }));
    }
    if (order != undefined) {
      if (responseParams) {
        const { order, history, dispatch, bankAccount, path } = this.props;
        const { orderId, orderContentTypeId } = order;
        const { negotiationDate, negotiationAmount } = responseParams;
        if (bankAccount) {
          dispatch(respondToOrder(orderId, orderContentTypeId, negotiationDate, negotiationAmount, () => history.push({ pathname: path, state: { orderId } })));
        }
      }
    }
  }

  componentWillReceiveProps(newProps) {
    const { dispatch, orderId } = newProps;
    if (newProps.orderId != this.props.orderId) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        ...OrderSummaryDao.PARTNER_ORDER_SUMMARY_DEFAULT_OPTIONS
      }));
    }
  }

  onOrderRespond = async ({ negotiationAmount, negotiationDate }) => {
    const { order, history, dispatch, bankAccount, parentPath, path } = this.props;
    const { orderId, orderContentTypeId } = order;
    if (bankAccount && typeof bankAccount === 'object' && Object.keys(bankAccount).length) {
      dispatch(respondToOrder(orderId, orderContentTypeId, negotiationDate, negotiationAmount, () => history.push({ pathname: path, state: { orderId } })));
    } else {
      const next = { pathname: path, state: { orderId, responseParams: { negotiationDate, negotiationAmount } }, transition: 'left' };
      // user has no bank account set up so take them to set it up
      history.push({ pathname: `${parentPath}/Settings/UpdateBankAccountDetails`, transition: 'left', state: { next } });
    }
  }

  render() {
    const { order, history, busy, orderId, errors } = this.props;
    const { onOrderRespond, resources } = this;
    const { InProgressControlsFactory } = resources;
    const InProgressControls = InProgressControlsFactory ? InProgressControlsFactory(order) : null;
    return busy || !order ? <LoadingScreen text={!busy && !order ? 'Order "' + orderId + '" cannot be found' : 'Loading Order...'} /> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack({ transition: 'right' })}>
            <Icon name='back-arrow' />
          </Button>
        </Left>
        <Body><Title>{order.title}</Title></Body>
      </Header>
      <Content>
        <ErrorRegion errors={errors} />
        <PartnerOrderLifecycleView isRatingCustomer={true} {...{ ...this.props, onOrderRespond, userCreatedThisOrder: false }}
          PlacedControls={[PartnerPriceSummary, PartnerNegotiationPanel, OrderSummary]}
          InProgressControls={InProgressControls}
          RejectedControls={[PartnerResponseRejected, OrderSummary]}
          AcceptedControls={InProgressControls}
          CompletedControls={[PartnerPriceSummary, RatingSummary, OrderSummary]}
          CancelledControls={[PartnerPriceSummary, OrderSummary]}
        />
      </Content>
    </Container>;
  }
}

//TODO - tidy up this mess
const PaymentStagesAndSummary = (props) => {
  const { history, path, orderId, width, height, order } = props;
  const goToTabNamed = (name) => {
    history.replace({ pathname: `${path}/${name}`, state: { orderId } });
  };
  const TabHeadings = [
    'Summary',
  ];
  if (order.orderStatus != 'ACCEPTED'){
    TabHeadings.push('Photos');
  }
  if (order.paymentStages && order.paymentStages.length){
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
  return [<Tabs key="1" initialPage={selecedTabIndex} page={selecedTabIndex}  {...shotgun.tabsStyle}>
    {TabHeadings.map(th =>  <Tab heading={getHeading(th)} key={th} onPress={() => goToTabNamed(th)} />)}
  </Tabs>,
  <ReduxRouter key="2" name="CustomerOrdersRouter" hideActionButtons={order.paymentType === 'DAYRATE'} {...props} images={images} height={height - shotgun.tabHeight} width={width} path={path} defaultRoute='Summary'>
    <Route path={'Summary'} component={OrderSummary} />
    <Route path={'PaymentStages'} component={PartnerStagedPaymentPanel} />
    <Route path={'Photos'} component={OrderProgressPictures} />
  </ReduxRouter>];
};


const getControlsFromOrder = (order) => {
  if (order.paymentType === 'DAYRATE') {
    return [PartnerPriceSummary, DayRatePersonellOrderInProgress, PaymentStagesAndSummary];
  }
  return [PartnerPriceSummary, FixedPersonellOrderInProgress,  PaymentStagesAndSummary];
};

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', () => 'Order Summary').
    delivery((order) => `${order.orderProduct.name} Delivery`).
    hire((order) => `${order.orderProduct.name}  Hire`).
    personell((order) => `${order.orderProduct.name} - ${order.orderProduct.name} Job`).
    rubbish((order) => `${order.orderProduct.name} Rubbish Collection`).
  property('PlacedControls', [PartnerNegotiationPanel, OrderSummary]).
  property('InProgressControlsFactory').
    personell(getControlsFromOrder).
    hire(() => [PartnerPriceSummary, HireOrderInProgress]).
    delivery(() => [PartnerPriceSummary, PartnerJourneyOrderInProgress]).
    rubbish(() => [PartnerPriceSummary, PartnerJourneyOrderInProgress]);
/*eslint-enable */


const mapStateToProps = (state, initialProps) => {
  const { orderId, responseParams } = getNavigationProps(initialProps);
  const order = findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');
  const user = getDaoState(state, ['user'], 'userDao');
  if (!user){
    return;
  }
  const { bankAccount } = user;
  const isPendingOrderSummarySubscription = isAnyOperationPending(state, [{ singleOrderSummaryDao: 'resetSubscription'}]);
  return {
    ...initialProps,
    errors: getOperationErrors(state, [{ partnerDao: 'acceptOrderRequest' }]) + '\n' + getAnyOperationError(state, 'orderDao') + '\n' + getAnyOperationError(state, 'singleOrderSummaryDao'),
    user,
    busyUpdating: isAnyOperationPending(state, [{ partnerDao: 'acceptOrderRequest' }, { partnerDao: 'updateOrderPrice' }]),
    busy: isPendingOrderSummarySubscription,
    order,
    orderId,
    responseParams,
    bankAccount
  };
};

export default withExternalState(mapStateToProps)(PartnerOrderDetail);

