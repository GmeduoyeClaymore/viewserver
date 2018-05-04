import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Image} from 'react-native';
import {Container, Header, Left, Button, Body, Title, Content, Text, Grid, Row, ListItem, View, Spinner, Col} from 'native-base';
import {Icon, LoadingScreen, ErrorRegion, SpinnerButton, CurrencyInput, Currency} from 'common/components';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors} from 'common/dao';
import * as ContentTypes from 'common/constants/ContentTypes';
import MapDetails from './MapDetails';
import invariant from 'invariant';
import moment from 'moment';
import {cancelOrder, rejectResponse, acceptResponse, updateOrderAmount} from 'customer/actions/CustomerActions';
import shotgun from 'native-base-theme/variables/shotgun';

const VISIBLE_STATUSES = ['RESPONDED', 'ACCEPTED'];
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
  return <SpinnerButton  {...rest} padded busy={busyUpdating} fullWidth danger onPress={onCancelOrder}><Text uppercase={false}>Cancel</Text></SpinnerButton> 
};

const  RejectPartner = ({orderId, partnerId, orderContentTypeId, busyUpdating, dispatch, ...rest}) => {
  const onRejectPartner = () => {
      dispatch(rejectResponse({orderId, partnerId, orderContentTypeId}));
  };
  return <SpinnerButton {...rest} padded busy={busyUpdating} fullWidth danger onPress={onRejectPartner}><Text uppercase={false}>Reject</Text></SpinnerButton>
};

const  AcceptPartner = ({orderId, partnerId, orderContentTypeId, busyUpdating, dispatch, ...rest}) => {
  const onAcceptPartner = () => {
      dispatch(acceptResponse({orderId, partnerId, orderContentTypeId}));
  };
  return <SpinnerButton  {...rest} padded busy={busyUpdating} fullWidth accept onPress={onAcceptPartner}><Text uppercase={false}>Accept</Text></SpinnerButton>
};

const  UpdateOrderPrice = ({orderId, orderContentTypeId, amount, onAmountUpdated, busyUpdating, dispatch, ...rest}) => {
  const onUpdateOrderAmount = () => {
    dispatch(updateOrderAmount({orderId, orderContentTypeId, amount}, onAmountUpdated));
  };
  return <SpinnerButton {...rest} padded busy={busyUpdating} fullWidth success onPress={onUpdateOrderAmount}><Text uppercase={false}>Update</Text></SpinnerButton>
};


const PartnerAcceptRejectControl = ({partnerResponses=[], orderId, orderContentTypeId, busyUpdating, dispatch}) => {
  return <Row>{partnerResponses.filter( res => !!~VISIBLE_STATUSES.indexOf(res.responseStatus)).map(
    response  => {
      const {partnerId, latitude, longitude, firstname, lastname, email, imageUrl, online, userStatus, statusMessage, ratingAvg, estimatedDate, price, responseStatus} = response;
      const stars = [...Array(ratingAvg)].map((e, i) => <Icon name='star' key={i} style={styles.star}/>);
      return <Row style={styles.view}>
          <Image  resizeMode='stretch' source={{uri: imageUrl}} style={styles.partnerImage}/>
          <Col  size={20} style={{marginLeft: 10}}>
          <Text style={{...styles.subHeading,marginBottom: 5}}>{firstname + ' ' + lastname}</Text>
            {!!~ratingAvg ? <Row style={{marginBottom: 8, marginTop: 8}}>{stars}</Row> : <Text>No Ratings</Text>}
            <Currency value={price} style={styles.price}/>
            <Text>{moment(parseInt(estimatedDate)).format('ddd Do MMMM, h:mma')}</Text>
          </Col>
          <Col size={20}>
            <AcceptPartner  busyUpdating={busyUpdating} style={{marginBottom:10}} orderId={orderId} orderContentTypeId={orderContentTypeId} partnerId={partnerId}  dispatch={dispatch}/>
            <RejectPartner  busyUpdating={busyUpdating} orderId={orderId} orderContentTypeId={orderContentTypeId} partnerId={partnerId} dispatch={dispatch}/>
          </Col>
      </Row>
    })}</Row>
}

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

  updateAmountInState = (amount) => {
    const {order} = this.props;
    super.setState({amount});
  }

  clearAmount = (amount) => {
    this.updateAmountInState(undefined);
    if(this.amountInput){
      this.amountInput.clear();
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
      <Content>
        <ErrorRegion errors={errors}/>
        <CancelOrder dispatch={dispatch} orderId={order.orderId} busyUpdating={busyUpdating} />
        <Grid style={{paddingLeft: 30, paddingTop: 10, marginBottom: 10}}>
          <Col size={32}>
            <Text style={{...styles.heading, marginTop:10}}>Advertised Rate</Text>
            <Row style={styles.row}>{!order.amount ? <Spinner/> : <Currency value={order.amount} style={styles.price} suffix={order.paymentType == 'DayRate' ?  ' a day' : ''}/>}</Row>
            <CurrencyInput ref={ip => {this.amountInput = ip;}} dispatch={dispatch} onValueChange={this.updateAmountInState} placeholder="Enter updated amount"/>
          </Col>
          {this.state.amount ? <Col size={20}>
            <UpdateOrderPrice style={{marginTop:17}} orderContentTypeId={order.orderContentTypeId} onAmountUpdated={this.clearAmount} dispatch={dispatch} orderId={order.orderId} amount={this.state.amount}/>
          </Col> : null}
        </Grid>
        <PartnerAcceptRejectControl dispatch={dispatch} orderId={order.orderId} orderContentTypeId={order.orderContentTypeId} partnerResponses={partnerResponses} busyUpdating={busyUpdating}/>
        <MapDetails order={order} client={client}/>
        <ListItem padded style={{borderBottomWidth: 0}}>
          <Grid>
            <Row><Text style={styles.itemDetailsTitle}>{this.resources.PageTitle()}</Text></Row>
            <Row><Text>{order.description}</Text></Row>
            {order.imageUrl !== undefined && order.imageUrl !== '' ?  <Row style={{justifyContent: 'center'}}><Image source={{uri: order.imageUrl}} resizeMode='contain' style={styles.image}/></Row> : null}
          </Grid>
        </ListItem>
      </Content>
    </Container>;
  }
}

const styles = {
  row: {
    marginTop:10,
    marginBottom:10
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
  }
};

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
  
  const errors = getOperationErrors(state, [{orderDao: 'cancelOrder'}, {orderDao: 'rejectResponse'}, {orderDao: 'updateOrderAmount'}])
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

