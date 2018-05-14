import React, {Component} from 'react';
import {TouchableOpacity} from 'react-native';
import {ListItem, View, Text, Grid, Row, Spinner, Col, Button} from 'native-base';
import {SpinnerButton, CurrencyInput, Currency, ValidatingButton, UserInfo} from 'common/components';
import moment from 'moment';
import {rejectResponse, acceptResponse, updateOrderAmount, cancelResponseCustomer} from 'customer/actions/CustomerActions';
import yup from 'yup';

const ACTIVE_NEGOTIATION_STATUSES = ['RESPONDED', 'ACCEPTED'];
const CAN_UPDATE_AMOUNT_STATUSES = ['PLACED'];

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
    const {partnerResponses, busyUpdating, dispatch} = this.props;
    const {showAll} = this.state;

    const filteredResponses = partnerResponses.filter(res => showAll || !!~ACTIVE_NEGOTIATION_STATUSES.indexOf(res.responseStatus));
    return filteredResponses.map(
      (response, idx)  => {
        const {partnerId, estimatedDate, price, responseStatus} = response;
        const canRespond = responseStatus === 'RESPONDED';

        return <ListItem key={idx} paddedTopBottom last={idx == filteredResponses.length - 1}>
          <Grid>
            <Row style={styles.headerRow}>
              <Currency value={price} style={styles.responseHeader}/>
              <Text> to start </Text>
              <Text style={styles.responseHeader}>{moment(parseInt(estimatedDate, 10)).format('ddd Do MMMM, h:mma')}</Text>
            </Row>
            <Row>
              <Col size={50}>
                <TouchableOpacity onPress={() => this.onUserInfoPress(partnerId)}>
                  <UserInfo dispatch={dispatch} user={response} imageWidth={60} showCallButton={false}/>
                </TouchableOpacity>
              </Col>
              <Col size={50}>
                {canRespond ? <SpinnerButton busy={busyUpdating} fullWidth style={styles.acceptButton} accept onPress={() => this.onAcceptPartner(partnerId)}>
                  <Text uppercase={false}>Accept</Text>
                </SpinnerButton> : null}

                {canRespond ? <SpinnerButton busy={busyUpdating} fullWidth danger onPress={() => this.onRejectPartner(partnerId)}>
                  <Text uppercase={false}>Reject</Text>
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

    return <View padded>
      <Grid style={styles.priceGrid}>
        <Col size={45}>
          <Text style={styles.heading}>{canUpdateOrderPrice ? 'Job advertised for' : 'Agreed Price'}</Text>
          {!order.amount ? <Spinner/> : <Currency value={order.amount} style={styles.price} suffix={order.paymentType == 'DayRate' ?  ' a day' : ''}/>}
        </Col>
        {canUpdateOrderPrice ?
          [<Col size={30} style={{justifyContent: 'center'}}>
            <CurrencyInput ref={ip => {this.amountInput = ip;}} style={styles.amountInput} dispatch={dispatch} onValueChanged={this.updateAmountInState} placeholder="Change price"/>
          </Col>,
          <Col size={25} style={{justifyContent: 'center'}}>
            <ValidatingButton busy={busyUpdating} fullWidth success validationSchema={validationSchema.amount} model={amount} onPress={this.onUpdateOrderAmount}>
              <Text uppercase={false}>Update</Text>
            </ValidatingButton>
          </Col>] : null}
      </Grid>

      {this.getPartnerResponses()}

      {hasRejected ? <Button onPress={this.toggleShow}><Text uppercase={false}>{showAll ? 'Hide rejected' : 'Show rejected'}</Text></Button> : null}
    </View>;
  }
}

const validationSchema = {
  amount: yup.number().required()
};

const styles = {
  amountInput: {
    fontSize: 12,
    padding: 0
  },
  toggleStage: {
    marginRight: 5,
    justifyContent: 'center',
    flex: 1
  },
  priceGrid: {
  },
  headerRow: {
    marginBottom: 10
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
    fontWeight: 'bold'
  }
};

