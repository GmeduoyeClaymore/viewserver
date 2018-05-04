import React, {Component} from 'react';
import {withExternalState} from 'custom-redux';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors, findOrderSummaryFromDao} from 'common/dao';
import {Text, Container, Header, Left, Button, Body, Title, Content} from 'native-base';
import {OrderSummary, PriceSummary, CurrencyInput, LoadingScreen, SpinnerButton, Icon} from 'common/components';
import {respondToOrder} from 'partner/actions/PartnerActions';
import * as ContentTypes from 'common/constants/ContentTypes';
import moment from 'moment';

class PartnerAvailableOrderDetail extends Component{
  constructor(props){
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  beforeNavigateTo(){
    const {dispatch, orderId, order} = this.props;
    if (order == undefined) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        reportId: 'partnerOrderDetail'
      }));
    }
  }

  setNegotiationAmount = (negotiationAmount) => {
    this.setState({negotiationAmount});
  }

  onRespondPress = async() => {
    const {order, history, dispatch, ordersRoot, bankAccount, parentPath, negotiationAmount} = this.props;
    const {orderId, requiredDate, orderContentTypeId} = order;
    const negotiationDate = moment(requiredDate).valueOf();


    if (bankAccount) {
      dispatch(respondToOrder(orderId, orderContentTypeId, negotiationDate, negotiationAmount,  () => history.push({pathname: `${ordersRoot}/PartnerMyOrders`, transition: 'left'})));
    } else {
      // user has no bank account set up so take them to set it up
      history.push({pathname: `${parentPath}/Settings/UpdateBankAccountDetails`, transition: 'left'}, {next: `${parentPath}/Checkout`});
    }
  }

  render() {
    const {order = {}, client, history, busy, busyUpdating} = this.props;

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
        <PriceSummary orderStatus={order.orderStatus} isPartner={true} price={order.amount}/>

        {this.resources.AllowsNegotiation ?
          [<Text note key='negotiationText'>or suggest a different price</Text>,
            <CurrencyInput key='negotiationAmount' onValueChange = {this.setNegotiationAmount} placeholder='Enter amount'/>] :
          null
        }

        <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.acceptButton} onPress={this.onRespondPress}><Text uppercase={false}>Request job</Text></SpinnerButton>

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
rubbish(true)
/*eslint-enable */

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  let order = findOrderSummaryFromDao(state, orderId, 'orderRequestDao');
  order = order || findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');

  return {
    ...initialProps,
    errors: getOperationErrors(state, [{ partnerDao: 'acceptOrderRequest'}]),
    user: getDaoState(state, ['user'], 'userDao'),
    busyUpdating: isAnyOperationPending(state, [{ partnerDao: 'acceptOrderRequest'}, {partnerDao: 'updateOrderPrice'}]),
    busy: !order,
    order,
    orderId,
    negotiationAmount: order ? order.amount : 0,
    bankAccount: getDaoState(state, ['bankAccount'], 'paymentDao')
  };
};

export default withExternalState(mapStateToProps)(PartnerAvailableOrderDetail);

