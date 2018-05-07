import React, {Component} from 'react';
import {Image} from 'react-native';
import {Text, Grid, Row, Spinner, Col} from 'native-base';
import {Icon, SpinnerButton, CurrencyInput, Currency} from 'common/components';
import * as ContentTypes from 'common/constants/ContentTypes';
import moment from 'moment';
import {rejectResponse, acceptResponse, updateOrderAmount} from 'customer/actions/CustomerActions';
import shotgun from 'native-base-theme/variables/shotgun';

export default class OrderNegotiationPanel extends Component{
  constructor(props) {
    super(props);
    this.state = {
      amount: undefined,
      showAll: false
    };
  }

  updateAmountInState = (amount) => {
    super.setState({amount});
  }

  toggleShow = () => {
    const {showAll} = this.state;
    super.setState({showAll: !showAll});
  }

  clearAmount = () => {
    this.updateAmountInState(undefined);
    if (this.amountInput){
      this.amountInput.clear();
    }
  }

  render(){
    const { order, partnerResponses, busyUpdating, dispatch} = this.props;
    if (!order || !partnerResponses){
      return null;
    }
    const canUpdateOrderPrice = !!~CAN_UPDATE_AMOUNT_STATUSES.indexOf(order.orderStatus);
    return [<Grid  key="1"  style={{paddingTop: 10, marginBottom: 10, paddingLeft: 10}}>
      <Col size={32}>
        <Text style={{...styles.heading, marginTop: 10}}>{canUpdateOrderPrice ? 'Advertised Rate' : 'Agreed Price'}</Text>
        <Row style={styles.row}>{!order.amount ? <Spinner/> : <Currency value={order.amount} style={styles.price} suffix={order.paymentType == 'DayRate' ?  ' a day' : ''}/>}</Row>
        { canUpdateOrderPrice ? <CurrencyInput ref={ip => {this.amountInput = ip;}} dispatch={dispatch} onValueChange={this.updateAmountInState} placeholder="Enter updated amount"/> : null}
      </Col>
      {this.state.amount && canUpdateOrderPrice ? <Col size={20}>
        <UpdateOrderPrice style={{marginTop: 17}} orderContentTypeId={order.orderContentTypeId} onAmountUpdated={this.clearAmount} dispatch={dispatch} orderId={order.orderId} amount={this.state.amount}/>
      </Col> : null}
    </Grid>,
    <PartnerAcceptRejectControl   key="2" showAll={this.state.showAll}  dispatch={dispatch} orderId={order.orderId} orderStatus={order.orderStatus} orderContentTypeId={order.orderContentTypeId} partnerResponses={partnerResponses} busyUpdating={busyUpdating}/>,
    partnerResponses.filter( res => !~ACTIVE_NEGOTIATION_STATUSES.indexOf(res.responseStatus)).length ? <Row style={{paddingLeft: 10}} key="3" onPress={this.toggleShow}><Text>{this.state.showAll ? 'Hide rejected' : 'Show rejected'}</Text></Row> : null];
  }
}

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


const  RejectPartner = ({orderId, partnerId, orderContentTypeId, busyUpdating, dispatch, style = {}, ...rest}) => {
  const onRejectPartner = () => {
    dispatch(rejectResponse({orderId, partnerId, orderContentTypeId}));
  };
  return <SpinnerButton {...rest} disabledStyle={{opacity: 0.1}} style={{...style, alignSelf: 'flex-start', flex: 1, marginRight: 0, marginLeft: 0, width: '100%'}}  padded busy={busyUpdating} fullWidth danger onPress={onRejectPartner}><Text uppercase={false}>Reject</Text></SpinnerButton>;
};

const  AcceptPartner = ({orderId, partnerId, orderContentTypeId, busyUpdating, dispatch, style = {}, ...rest}) => {
  const onAcceptPartner = () => {
    dispatch(acceptResponse({orderId, partnerId, orderContentTypeId}));
  };
  return <SpinnerButton  {...rest} disabledStyle={{opacity: 0.1}} style={{...style, alignSelf: 'flex-start', flex: 1, marginRight: 0, marginLeft: 0, width: '100%'}} padded busy={busyUpdating} fullWidth accept onPress={onAcceptPartner}><Text uppercase={false}>Accept</Text></SpinnerButton>;
};

const  UpdateOrderPrice = ({orderId, orderContentTypeId, amount, onAmountUpdated, busyUpdating, dispatch, style = {},  ...rest}) => {
  const onUpdateOrderAmount = () => {
    dispatch(updateOrderAmount({orderId, orderContentTypeId, amount}, onAmountUpdated));
  };
  return <SpinnerButton {...rest} padded busy={busyUpdating} style={{...style, alignSelf: 'flex-end', flex: 1, maxHeight: 45,  marginRight: 0, marginLeft: 0}}  fullWidth success onPress={onUpdateOrderAmount}><Text uppercase={false}>Update</Text></SpinnerButton>;
};

const PartnerAcceptRejectControl = ({partnerResponses = [], orderId, orderStatus, orderContentTypeId, busyUpdating, dispatch, showAll}) => {
  return <Col>{partnerResponses.filter( res => showAll || !!~ACTIVE_NEGOTIATION_STATUSES.indexOf(res.responseStatus)).map(
    (response, idx)  => {
      const {partnerId, latitude, longitude, firstName, lastName, email, imageUrl, online, userStatus, statusMessage, ratingAvg, estimatedDate, price, responseStatus} = response;
      const canRespond = !!~CAN_UPDATE_AMOUNT_STATUSES.indexOf(orderStatus) && responseStatus === 'RESPONDED';
      const stars = [...Array(ratingAvg)].map((e, i) => <Icon name='star' key={i} style={styles.star}/>);
      const imageStyle = styles.partnerImage;
      const statusStyle = ResponseStatusStyles[responseStatus] || ResponseStatusStyles.REJECTED;
      return <Row key={idx} style={{...styles.view, marginBottom: 10}}>
        <Image style={{...imageStyle, ...statusStyle}}  resizeMode='stretch' source={{uri: imageUrl}}/>
        <Col  size={50} style={{marginLeft: 10}}>
          <Text style={{...styles.subHeading, marginBottom: 5}}>{firstName + ' ' + lastName}</Text>
          {!!~ratingAvg ? <Row style={{marginBottom: 8, marginTop: 8}}>{stars}</Row> : <Text>No Ratings</Text>}
          <Currency value={price} style={styles.price}/>
          <Text>{moment(parseInt(estimatedDate, 10)).format('ddd Do MMMM, h:mma')}</Text>
        </Col>
        <Col size={23}>
          <AcceptPartner disabled={!canRespond} busyUpdating={busyUpdating} style={{marginBottom: 10}} orderId={orderId} orderContentTypeId={orderContentTypeId} partnerId={partnerId}  dispatch={dispatch}/>
          <RejectPartner disabled={!canRespond}  busyUpdating={busyUpdating} orderId={orderId} orderContentTypeId={orderContentTypeId} partnerId={partnerId} dispatch={dispatch}/>
        </Col>
      </Row>;
    })}</Col>;
};

const styles = {
  row: {
    marginTop: 10,
    marginBottom: 10
  },
  toggleStage: {
    marginRight: 5,
    justifyContent: 'center',
    flex: 1
  },
  view: {
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

