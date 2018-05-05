import React, {Component} from 'react';
import {withExternalState} from 'custom-redux';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors, findOrderSummaryFromDao, getAnyOperationError} from 'common/dao';
import {Text, Container, Header, Left, Button, Body, Title, Content} from 'native-base';
import {OrderSummary, PriceSummary, LoadingScreen, Icon, ErrorRegion} from 'common/components';
import {respondToOrder} from 'partner/actions/PartnerActions';
import * as ContentTypes from 'common/constants/ContentTypes';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import PartnerNegotiationPanel from './PartnerNegotiationPanel';
import PartnerPaymentStagesPanel from './PartnerPaymentStagesPanel';
import moment from 'moment';

class PartnerAvailableOrderDetail extends Component{
  constructor(props){
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  beforeNavigateTo(){
    const {dispatch, orderId, order, responseParams} = this.props;
    dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
      orderId,
      ...OrderSummaryDao.PARTNER_ORDER_SUMMARY_DEFAULT_OPTIONS
    }));
    if (order != undefined) {
      if (responseParams){
        const {order, history, dispatch, bankAccount, path} = this.props;
        const {orderId,  orderContentTypeId} = order;
        const {negotiationDate, negotiationAmount} = responseParams;
        if (bankAccount) {
          dispatch(respondToOrder(orderId, orderContentTypeId, negotiationDate, negotiationAmount,  () => history.push({pathname: path, state: {orderId}})));
        }
      }
    }
  }

  onRespondPress = async({negotiationAmount, negotiationDate}) => {
    const {order, history, dispatch, bankAccount, parentPath, path} = this.props;
    const {orderId, orderContentTypeId} = order;
    if (bankAccount) {
      dispatch(respondToOrder(orderId, orderContentTypeId, negotiationDate, negotiationAmount,  () => history.push({pathname: path, state: {orderId}})));
    } else {
      const next = {pathname: path, state: {orderId, responseParams: {negotiationDate, negotiationAmount} }, transition: 'left'};
      // user has no bank account set up so take them to set it up
      history.push({pathname: `${parentPath}/Settings/UpdateBankAccountDetails`, transition: 'left', state: {next}});
    }
  }

  render() {
    const {order = {}, client, history, busy, busyUpdating, dispatch, errors} = this.props;
    const {responseInfo = {}, negotiatedResponseStatus} = order;
    const negotiationAmount = this.props.negotiationAmount || order.amount;
    const negotiationDate = this.props.negotiationDate || order.requiredDate;

    return busy ? <LoadingScreen text='Waiting for order'/> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack({transition: 'right'})}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Order Summary</Title></Body>
      </Header>
      <Content>
        <ErrorRegion errors={errors}/>
        <PriceSummary orderStatus={order.orderStatus} isPartner={true} price={order.amount}/>
        {this.resources.AllowsNegotiation ? <PartnerNegotiationPanel busyUpdating={busyUpdating} negotiatedResponseStatus={negotiatedResponseStatus} negotiationAmount={negotiationAmount} responseInfo={responseInfo} negotiationDate={negotiationDate} onOrderRespond={this.onRespondPress}/> : null}
        {this.resources.AllowsStagedPayments ? <PartnerPaymentStagesPanel negotiatedResponseStatus={negotiatedResponseStatus}  order={order} busyUpdating={busyUpdating} dispatch={dispatch}/> : null}
        <OrderSummary order={order} client={client}/>
      </Content>
    </Container>;
  }
}

const styles = {
  suggestText: {
    width: '100%',
    alignSelf: 'stretch',
    justifyContent: 'center',
    textAlign: 'center',
    padding: 10
  },
  acceptButton: {
    marginTop: 20,
    marginBottom: 10
  }
};

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
property('AllowsNegotiation', false).
  personell(true).
  rubbish(true).
property('AllowsStagedPayments', false).
  personell(true)
/*eslint-enable */

const mapStateToProps = (state, initialProps) => {
  const {orderId, responseParams} = getNavigationProps(initialProps);
  const order = findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');
  const user = getDaoState(state, ['user'], 'userDao');
  const {bankAccount} = user;
  return {
    ...initialProps,
    errors: getOperationErrors(state, [{ partnerDao: 'acceptOrderRequest'}]) + '\n' + getAnyOperationError(state, 'orderDao') + '\n' + getAnyOperationError(state, 'singleOrderSummaryDao'),
    user,
    busyUpdating: isAnyOperationPending(state, [{ partnerDao: 'acceptOrderRequest'}, {partnerDao: 'updateOrderPrice'}]),
    busy: !order,
    order,
    orderId,
    responseParams,
    bankAccount
  };
};

export default withExternalState(mapStateToProps)(PartnerAvailableOrderDetail);

