import React, {Component} from 'react';
import {Image} from 'react-native';
import {ListItem, View, Text, Grid, Row, Spinner, Col, Button, Item, Label} from 'native-base';
import {SpinnerButton, CurrencyInput, Currency, ValidatingButton, AverageRating} from 'common/components';
import moment from 'moment';
import {rejectResponse, acceptResponse, updateOrderAmount, cancelResponseCustomer, updateOrderVisibility} from 'customer/actions/CustomerActions';
import yup from 'yup';
import shotgun from 'native-base-theme/variables/shotgun';


const ACTIVE_NEGOTIATION_STATUSES = ['RESPONDED', 'ACCEPTED'];
const CAN_UPDATE_AMOUNT_STATUSES = ['PLACED'];

const ResponseStatusStyles = {
  RESPONDED: shotgun.brandWarning,
  ACCEPTED: shotgun.brandSuccess,
  REJECTED: shotgun.brandDanger
};

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

  onUpdateOrderAmount = () => {
    const {order, dispatch} = this.props;
    const {orderId, orderContentTypeId} = order;
    const {amount} = this.state;
    dispatch(updateOrderAmount({orderId, orderContentTypeId, amount}, this.clearAmount));
  }

  showOrderToEveryone = () => {
    const {order, dispatch} = this.props;
    const {orderId, orderContentTypeId} = order;
    dispatch(updateOrderVisibility({orderId, orderContentTypeId, justForFriends: false}, this.clearAmount));
  }

  onUserInfoPress = (partnerId) => {
    const {ordersRoot, history} = this.props;

    history.push({
      pathname: `${ordersRoot}/UserDetail`,
      state: {userId: partnerId},
      transition: 'bottom'
    });
  }

  onRejectPartner = (partnerId) => {
    const {order, dispatch} = this.props;
    const {orderId, orderContentTypeId} = order;
    dispatch(rejectResponse({orderId, partnerId, orderContentTypeId}));
  };

  onAcceptPartner = (partnerId) => {
    const {order, dispatch} = this.props;
    const {orderId, orderContentTypeId} = order;
    dispatch(acceptResponse({orderId, partnerId, orderContentTypeId}));
  };

  onCancelResponse = (partnerId) => {
    const {order, dispatch} = this.props;
    const {orderId, orderContentTypeId} = order;
    dispatch(cancelResponseCustomer(orderId, orderContentTypeId, partnerId));
  };

  getPartnerResponses = () => {
    const {partnerResponses, busyUpdating, order} = this.props;
    const {showAll} = this.state;

    if (!partnerResponses  || !partnerResponses.filter){
      return null;
    }

    const filteredResponses = partnerResponses.filter(res => showAll || !!~ACTIVE_NEGOTIATION_STATUSES.indexOf(res.responseStatus));
    return filteredResponses.map(
      (response, idx)  => {
        const {partnerId, estimatedDate, price, responseStatus, firstName, lastName, imageUrl, ratingAvg} = response;
        const canRespond = responseStatus === 'RESPONDED';
        const statusColor = ResponseStatusStyles[responseStatus] || ResponseStatusStyles.REJECTED;

        return <ListItem key={idx} padded paddedTopBottomNarrow>
          <Grid>
            <Row onPress={() => this.onUserInfoPress(partnerId)}>
              {imageUrl && imageUrl != 'null' ? <Image source={{uri: imageUrl}} resizeMode='contain' style={styles.reponseImage}/> : null}
              <Text style={[styles.responseHeader, {marginRight: 10, color: statusColor}]}>{`${firstName} ${lastName}`}</Text>
              <AverageRating rating={ratingAvg}/>
            </Row>
            <Row>
              <Col size={70}>
                <Item stackedLabel last style={{paddingLeft: 0, marginLeft: 0}}>
                  <Label style={{fontSize: 12, paddingTop: 5, marginTop: 5}}>Amount</Label>
                  <Currency style={styles.responseHeader} value={price} suffix={order.paymentType == 'DayRate' ?  ' a day' : ''}/>
                </Item>

                <Item stackedLabel last style={{paddingLeft: 0, marginLeft: 0}}>
                  <Label style={{fontSize: 12}}>Start date</Label>
                  <Text>{moment(parseInt(estimatedDate, 10)).format('ddd Do MMMM, h:mma')}</Text>
                </Item>
              </Col>

              <Col size={30}>
                {canRespond ? <SpinnerButton busy={busyUpdating} fullWidth style={styles.acceptButton} accept onPress={() => this.onAcceptPartner(partnerId)}>
                  <Text uppercase={false}>Accept</Text>
                </SpinnerButton> : null}

                {canRespond ? <SpinnerButton busy={busyUpdating} fullWidth danger onPress={() => this.onRejectPartner(partnerId)}>
                  <Text uppercase={false}>Decline</Text>
                </SpinnerButton> : null}

                {responseStatus === 'ACCEPTED' ?
                  <SpinnerButton busy={busyUpdating} danger fullWidth onPress={() => this.onCancelResponse(partnerId)}>
                    <Text uppercase={false}>Reject</Text>
                  </SpinnerButton> : null}
              </Col>
            </Row>
          </Grid>
        </ListItem>;
      });
  };

  render(){
    const {order, partnerResponses, busyUpdating, dispatch} = this.props;
    const {amount, showAll} = this.state;
    const hasRejected = partnerResponses.filter( res => !~ACTIVE_NEGOTIATION_STATUSES.indexOf(res.responseStatus)).length > 0;

    if (!order || !partnerResponses){
      return null;
    }
    const canUpdateOrderPrice = !!~CAN_UPDATE_AMOUNT_STATUSES.indexOf(order.orderStatus);

    return <View>
      <Grid style={styles.priceGrid}>
        <Row style={styles.row}>
          <Text style={styles.heading}>{canUpdateOrderPrice ? 'Job advertised for' : 'Agreed Price'}</Text>
        </Row>
        <Row style={styles.row}>
          {!order.amount ? <Spinner/> : <Currency value={order.amount} style={styles.price} suffix={order.paymentType == 'DayRate' ?  ' a day' : ''}/>}
        </Row>
        {canUpdateOrderPrice ?
          <Row style={styles.changePriceRow}>
            <Col size={70} style={styles.changePriceColumn}>
              <CurrencyInput ref={ip => {this.amountInput = ip;}} dispatch={dispatch} onValueChanged={this.updateAmountInState} placeholder="Enter new price"/>
            </Col>
            <Col size={30} style={styles.changePriceColumn}>
              <ValidatingButton busy={busyUpdating} fullWidth success validationSchema={validationSchema.amount} model={amount} onPress={this.onUpdateOrderAmount}>
                <Text uppercase={false}>Update</Text>
              </ValidatingButton>
            </Col>
          </Row> : null}
        {order.justForFriends ? <SpinnerButton style={{marginTop: 10}} busy={busyUpdating} info fullWidth onPress={() => this.showOrderToEveryone()}><Text uppercase={false}>Advertise to Everyone</Text></SpinnerButton> : null}
      </Grid>

      {this.getPartnerResponses()}

      {hasRejected ? <Button onPress={this.toggleShow} padded light style={{alignSelf: 'flex-end', marginTop: 15, height: 35}}><Text uppercase={false}>{showAll ? '- Hide declined' : '+ Show declined'}</Text></Button> : null}
    </View>;
  }
}

const validationSchema = {
  amount: yup.number().required()
};

const styles = {
  toggleStage: {
    marginRight: 5,
    justifyContent: 'center',
    flex: 1
  },
  priceGrid: {
    paddingBottom: shotgun.contentPadding,
    paddingLeft: shotgun.contentPadding,
    paddingRight: shotgun.contentPadding,
    borderBottomWidth: shotgun.listItemBorderWidth,
    borderColor: shotgun.listBorderColor
  },
  changePriceRow: {
    marginTop: 15
  },
  changePriceColumn: {
    justifyContent: 'center'
  },
  row: {
    justifyContent: 'center'
  },
  heading: {
    fontSize: 16,
    alignSelf: 'center'
  },
  subHeading: {
    fontSize: 14,
    fontWeight: 'bold'

  },
  buttonText: {
    fontSize: 10
  },
  acceptButton: {
    marginBottom: 10
  },
  price: {
    fontSize: 30,
    lineHeight: 34,
    fontWeight: 'bold'
  },
  responseHeader: {
    fontSize: 18,
    fontWeight: 'bold',
    marginRight: 10
  },
  reponseImage: {
    aspectRatio: 1,
    borderRadius: shotgun.imageBorderRadius,
    marginRight: 10,
    width: 30,
    alignSelf: 'center'
  }
};

