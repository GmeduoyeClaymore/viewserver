import React, { Component } from 'react';
import { withExternalState, ReduxRouter, Route } from 'custom-redux';
import { resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors, findOrderSummaryFromDao, getAnyOperationError } from 'common/dao';
import { Container, Header, Left, Button, Body, Title, Content, Tab, Row, Text } from 'native-base';
import { OrderSummary, LoadingScreen, Icon, ErrorRegion, RatingSummary, Tabs } from 'common/components';
import PartnerOrderLifecycleView from 'common/components/orders/PartnerOrderLifecycleView';
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

const ResponseRejected = ({ order }) => (<Row style={{ padding: 5, marginBottom: 5 }}><Icon name='star' style={styles.starDeclined} /><Text style={{ paddingTop: 15, paddingLeft: 5 }}>{`${order.customer.firstName} ${order.customer.lastName} Rejected`}</Text></Row>);

class PartnerOrderDetail extends Component {
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  beforeNavigateTo() {
    const { dispatch, orderId, order, responseParams } = this.props;
    dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
      orderId,
      ...OrderSummaryDao.PARTNER_ORDER_SUMMARY_DEFAULT_OPTIONS
    }));
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
          RejectedControls={[ResponseRejected, OrderSummary]}
          AcceptedControls={InProgressControls}
          CompletedControls={[PartnerPriceSummary, RatingSummary, OrderSummary]}
          CancelledControls={[PartnerPriceSummary, OrderSummary]}
        />
      </Content>
    </Container>;
  }
}

const styles = {
  starDeclined: {
    paddingTop: 10,
    alignSelf: 'center',
    fontSize: 30,
    padding: 2,
    color: shotgun.brandDanger,
  }
};

const PaymentStagesAndSummary = (props) => {
  const { history, path, orderId, width, height, order } = props;
  const goToTabNamed = (name) => {
    history.replace({ pathname: `${path}/${name}`, state: { orderId } });
  };
  const paymentTabHeading = order.paymentType !== 'DAYRATE' ? 'Payment Stages' : 'Days Worked';
  return [<Tabs key="1" initialPage={history.location.pathname.endsWith('PaymentStages') ? 1 : 0} page={history.location.pathname.endsWith('PaymentStages') ? 1 : 0}  {...shotgun.tabsStyle}>
    <Tab heading='Summary' onPress={() => goToTabNamed('Summary')} />
    {order.paymentStages && order.paymentStages.length ? <Tab heading={paymentTabHeading} onPress={() => goToTabNamed('PaymentStages')} /> : null}
  </Tabs>,
  <ReduxRouter key="2" name="CustomerOrdersRouter" {...props} height={height - shotgun.tabHeight} width={width} path={path} defaultRoute='Summary'>
    <Route path={'Summary'} component={OrderSummary} />
    <Route path={'PaymentStages'} component={PartnerStagedPaymentPanel} />
  </ReduxRouter>];
};


const getControlsFromOrder = (order) => {
  if (order.paymentType === 'DAYRATE') {
    return [PartnerPriceSummary, DayRatePersonellOrderInProgress, PaymentStagesAndSummary];
  }
  return [PartnerPriceSummary, FixedPersonellOrderInProgress, PaymentStagesAndSummary];
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

