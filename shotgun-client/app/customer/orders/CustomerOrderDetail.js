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


const ACTIVE_NEGOTIATION_STATUSES = ['RESPONDED', 'ACCEPTED'];
const CAN_UPDATE_AMOUNT_STATUSES = ['PLACED'];
const rejectedImageStyle = {
  borderColor: shotgun.brandDanger,
  borderWidth: 4
};
const respondedImageStyle = {
  borderColor: shotgun.brandWarning,
  borderWidth: 4
};

const acceptedImageStyle = {
  borderColor: shotgun.brandSuccess,
  borderWidth: 4
};

const ResponseStatusStyles = {
  RESPONDED: respondedImageStyle,
  ACCEPTED: acceptedImageStyle,
  REJECTED: rejectedImageStyle
};
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
  return <SpinnerButton  {...rest} disabledStyle={{opacity: 0.1}}  padded busy={busyUpdating} fullWidth danger onPress={onCancelOrder}><Text uppercase={false}>Cancel</Text></SpinnerButton> 
};

const  RejectPartner = ({orderId, partnerId, orderContentTypeId, busyUpdating, dispatch, ...rest}) => {
  const onRejectPartner = () => {
      dispatch(rejectResponse({orderId, partnerId, orderContentTypeId}));
  };
  return <SpinnerButton {...rest} disabledStyle={{opacity: 0.1}}  padded busy={busyUpdating} fullWidth danger onPress={onRejectPartner}><Text uppercase={false}>Reject</Text></SpinnerButton>
};

const  AcceptPartner = ({orderId, partnerId, orderContentTypeId, busyUpdating, dispatch, ...rest}) => {
  const onAcceptPartner = () => {
      dispatch(acceptResponse({orderId, partnerId, orderContentTypeId}));
  };
  return <SpinnerButton  {...rest} disabledStyle={{opacity: 0.1}} padded busy={busyUpdating} fullWidth accept onPress={onAcceptPartner}><Text uppercase={false}>Accept</Text></SpinnerButton>
};

const  UpdateOrderPrice = ({orderId, orderContentTypeId, amount, onAmountUpdated, busyUpdating, dispatch, ...rest}) => {
  const onUpdateOrderAmount = () => {
    dispatch(updateOrderAmount({orderId, orderContentTypeId, amount}, onAmountUpdated));
  };
  return <SpinnerButton {...rest} padded busy={busyUpdating} fullWidth success onPress={onUpdateOrderAmount}><Text uppercase={false}>Update</Text></SpinnerButton>
};


const PartnerAcceptRejectControl = ({partnerResponses=[], orderId, orderStatus, orderContentTypeId, busyUpdating, dispatch, showAll}) => {
  return <Col>{partnerResponses.filter( res => showAll || !!~ACTIVE_NEGOTIATION_STATUSES.indexOf(res.responseStatus)).map(
    (response,idx)  => {
      const {partnerId, latitude, longitude, firstname, lastname, email, imageUrl, online, userStatus, statusMessage, ratingAvg, estimatedDate, price, responseStatus} = response;
      const canRespond = !!~CAN_UPDATE_AMOUNT_STATUSES.indexOf(orderStatus) && 'RESPONDED' === responseStatus;
      const stars = [...Array(ratingAvg)].map((e, i) => <Icon name='star' key={i} style={styles.star}/>);
      const imageStyle = styles.partnerImage;
      const statusStyle = ResponseStatusStyles[responseStatus] || ResponseStatusStyles.REJECTED
      return <Row key={idx} style={{...styles.view, marginBottom:10}}>
          <Image style={{...imageStyle,...statusStyle}}  resizeMode='stretch' source={{uri: imageUrl}}/>
          <Col  size={20} style={{marginLeft: 10}}>
          <Text style={{...styles.subHeading,marginBottom: 5}}>{firstname + ' ' + lastname}</Text>
            {!!~ratingAvg ? <Row style={{marginBottom: 8, marginTop: 8}}>{stars}</Row> : <Text>No Ratings</Text>}
            <Currency value={price} style={styles.price}/>
            <Text>{moment(parseInt(estimatedDate)).format('ddd Do MMMM, h:mma')}</Text>
          </Col>
          <Col size={20}>
            <AcceptPartner disabled={!canRespond} busyUpdating={busyUpdating} style={{marginBottom:10}} orderId={orderId} orderContentTypeId={orderContentTypeId} partnerId={partnerId}  dispatch={dispatch}/>
            <RejectPartner disabled={!canRespond}  busyUpdating={busyUpdating} orderId={orderId} orderContentTypeId={orderContentTypeId} partnerId={partnerId} dispatch={dispatch}/>
          </Col>
      </Row>
    })}</Col>
}

class OrderNegotiationPanel extends Component{
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
    this.state = {
      amount: undefined,
      showAll: false
    }
  }


  updateAmountInState = (amount) => {
    const {order} = this.props;
    super.setState({amount});
  }

  toggleShow = () => {
    const {showAll} = this.state;
    super.setState({showAll: !showAll});
  }

  clearAmount = (amount) => {
    this.updateAmountInState(undefined);
    if(this.amountInput){
      this.amountInput.clear();
    }
  }

  render(){
    const {busy, order, orderId, client, partnerResponses, errors, history, busyUpdating, dispatch} = this.props;
    if(!order){
      return null;
    }
    const canUpdateOrderPrice = !!~CAN_UPDATE_AMOUNT_STATUSES.indexOf(order.orderStatus);
    return [<Grid style={{paddingLeft: 30, paddingTop: 10, marginBottom: 10}}>
            <Col size={32}>
              <Text style={{...styles.heading, marginTop:10}}>Advertised Rate</Text>
              <Row style={styles.row}>{!order.amount ? <Spinner/> : <Currency value={order.amount} style={styles.price} suffix={order.paymentType == 'DayRate' ?  ' a day' : ''}/>}</Row>
              { canUpdateOrderPrice ? <CurrencyInput ref={ip => {this.amountInput = ip;}} dispatch={dispatch} onValueChange={this.updateAmountInState} placeholder="Enter updated amount"/> : null}
            </Col>
            {this.state.amount && canUpdateOrderPrice? <Col size={20}>
              <UpdateOrderPrice style={{marginTop:17}} orderContentTypeId={order.orderContentTypeId} onAmountUpdated={this.clearAmount} dispatch={dispatch} orderId={order.orderId} amount={this.state.amount}/>
            </Col> : null}
          </Grid>
          ,<PartnerAcceptRejectControl showAll={this.state.showAll}  dispatch={dispatch} orderId={order.orderId} orderStatus={order.orderStatus} orderContentTypeId={order.orderContentTypeId} partnerResponses={partnerResponses} busyUpdating={busyUpdating}/>, 
        partnerResponses.length ? <Row style={{paddingLeft: 25}} onPress={this.toggleShow}><Text>{this.state.showAll ? "Hide rejected": "Show rejected"}</Text></Row> : null];
  }
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
        <OrderNegotiationPanel {...this.props}/>
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

