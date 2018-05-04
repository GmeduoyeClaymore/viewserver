import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Image} from 'react-native';
import {Container, Header, Left, Button, Body, Title, Content, Text, Grid, Row, ListItem} from 'native-base';
import {Icon, LoadingScreen, ErrorRegion, SpinnerButton, OrderSummary} from 'common/components';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors} from 'common/dao';
import * as ContentTypes from 'common/constants/ContentTypes';
import MapDetails from './MapDetails';
import {cancelOrder} from 'customer/actions/CustomerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import CustomerNegotiationPanel from './CustomerNegotiationPanel';
import CustomerStagedPaymentPanel from './CustomerStagedPaymentPanel';

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', () => 'Order Summary').
    delivery(() => 'Delivery Job').
    rubbish(() => 'Rubbish Collection');
/*eslint-disable */

const  CancelOrder = ({orderId, busyUpdating, dispatch, ...rest}) => {
  const onCancelOrder = () => {
      dispatch(cancelOrder({orderId}));
  };
  return <SpinnerButton  {...rest} disabledStyle={{opacity: 0.1}}  busy={busyUpdating} fullWidth danger onPress={onCancelOrder}><Text uppercase={false}>Cancel</Text></SpinnerButton> 
};

class CustomerOrderDetail extends Component{
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
    this.state = {
      amount: undefined
    }
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
    const {busy, order, orderId, client, partnerResponses, errors, history, busyUpdating, dispatch} = this.props;
    const {resources} = this;
    return busy || !order? <LoadingScreen text={ !busy && !order ? "Order \"" + orderId + "\" cannot be found" : "Loading Order..."}/> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{resources.PageTitle(order)}</Title></Body>
      </Header>
      <Content padded>
        <ErrorRegion errors={errors}/>
        <CancelOrder dispatch={dispatch} orderId={order.orderId} busyUpdating={busyUpdating} />
        <CustomerNegotiationPanel {...this.props}/>
        <CustomerStagedPaymentPanel {...this.props}/>
        <OrderSummary order={order} client={client}/>
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
  if(orderId == null){
    return;
  }
  let order = findOrderSummaryFromDao(state,orderId,'orderSummaryDao');
  order = order || findOrderSummaryFromDao(state,orderId,'singleOrderSummaryDao');

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

export default connect(
  mapStateToProps
)(CustomerOrderDetail);


const styles = {
  row: {
    marginTop:10,
    marginBottom:10
  },
  toggleStage: {
    marginRight: 5,
    justifyContent: 'center',
    flex: 1
  },
  view: {
    marginLeft: 30,
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    marginTop: 5
  },
  heading: {
    fontSize: 16
  },
  subHeading: {
    fontSize: 14,
    fontWeight: 'bold'

  },
  buttonText: {
    fontSize: 10
  },
  text: {
    marginRight: 5
  },
  star: {
    fontSize: 15,
    padding: 2,
    color: shotgun.gold,
  },
  partnerImage: {
    aspectRatio: 1,
    width: 80,
    height: 80,
  },
  price: {
    fontSize: 30,
    lineHeight: 34,
    fontWeight: 'bold'
  }, 
  smlprice: {
    fontSize: 15,
    fontWeight: 'bold'
  }
};


