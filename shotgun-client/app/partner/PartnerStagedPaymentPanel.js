import React, { Component } from 'react';
import {Platform} from 'react-native';
import { Text, Row, Col, Spinner, View, ListItem} from 'native-base';
import { SpinnerButton, Currency, Icon } from 'common/components';
import { completePaymentStage, startPaymentStage } from 'partner/actions/PartnerActions';
import shotgun from 'native-base-theme/variables/shotgun';

const IS_ANDROID = Platform.OS === 'android';
const CAN_START_PAYMENT_STAGE_STATUS = ['None'];
const CAN_COMPLETE_PAYMENT_STAGE_STATUS = ['Started'];
const CAN_START_PAYMENT_STAGE_ORDER_STATUS = ['STARTED', 'ASSIGNED'];

export default class PartnerStagedPaymentPanel extends Component {
  constructor(props) {
    super(props);
    this.state = {
      paymentStageType: 'Percentage'
    };
  }

  setPaymentStageType = (paymentStageType) => {
    this.setState({ paymentStageType });
  }

  onStartPaymentStage = (paymentStageId) => {
    const {dispatch, order} = this.props;
    const {orderId, orderContentTypeId} = order;
    dispatch(startPaymentStage({orderId, paymentStageId, orderContentTypeId }));
  };

  onCompletePaymentStage = (paymentStageId) => {
    const {dispatch, order} = this.props;
    const {orderId, orderContentTypeId} = order;
    dispatch(completePaymentStage({ orderId, paymentStageId, orderContentTypeId }));
  };

  getPaymentStages = () => {
    const {order, busyUpdating} = this.props;
    const {paymentStages = [], negotiatedOrderStatus, amount} = order;

    const hasStartedStage = paymentStages.find(c => c.paymentStageStatus === 'Started');
    return paymentStages.map(
      (paymentStage, idx) => {
        const {quantity, name, description, paymentStageType, paymentStageStatus, id} = paymentStage;
        const isPercent = paymentStageType === 'Percentage';
        const currencyTotal = isPercent ? amount * (quantity / 100) : quantity;

        return <ListItem key={idx} style={styles.paymentStageRow}>
          <Row>
            <Col size={35}>
              <Text style={styles.subHeading}>{name}</Text>
              <Text style={styles.description}>{description}</Text>
            </Col>
            <Col size={35} style={styles.amountColumn}>
              <Currency value={currencyTotal} style={styles.smlprice}/>
              {isPercent ? <Text style={styles.amountPercentage}>{` (${quantity}%)`}</Text> : null}
            </Col>
            <Col size={30} >
              {!!~CAN_START_PAYMENT_STAGE_STATUS.indexOf(paymentStageStatus) && !!~CAN_START_PAYMENT_STAGE_ORDER_STATUS.indexOf(negotiatedOrderStatus) && !hasStartedStage ?
                <SpinnerButton busy={busyUpdating} style={[styles.stageButton, {marginBottom: 10}]} padded fullWidth success onPress={() => this.onStartPaymentStage(id)}>
                  <Text uppercase={false}>Start</Text>
                </SpinnerButton> :
                null}

              {!!~CAN_COMPLETE_PAYMENT_STAGE_STATUS.indexOf(paymentStageStatus) && !!~CAN_START_PAYMENT_STAGE_ORDER_STATUS.indexOf(negotiatedOrderStatus) ?
                <SpinnerButton busy={busyUpdating} style={styles.stageButton} fullWidth padded success onPress={() => this.onCompletePaymentStage(id)}>
                  <Text uppercase={false} adjustsFontSizeToFit allowFontScaling>Complete</Text>
                </SpinnerButton> :
                null}

              {paymentStageStatus === 'Complete' ?  [<Spinner  size={ IS_ANDROID ? 25 : 1} key='spinner' color={shotgun.brandSuccess} style={styles.waitingSpinner}/>, <Text key='text' numberOfLines={1} style={[styles.successText, {fontSize: 12}]}>Awaiting Payment</Text>] : null}
              {paymentStageStatus === 'Paid' ? [<Icon  name='checkmark' style={styles.checkmark}/>, <Text key='text2' style={styles.successText}>Paid</Text>] : null}
            </Col>
          </Row>
        </ListItem>;
      });
  }

  render() {
    const { order} = this.props;
    const { paymentStages = [] } = order;

    if (!order || !paymentStages.length) {
      return null;
    }
    return <View padded>
      <Text style={styles.heading}>Payment Stages</Text>
      {this.getPaymentStages()}
    </View>;
  }
}

const styles = {
  paymentStageRow: {
    marginBottom: 10,
    paddingBottom: 15
  },
  amountColumn: {
    alignContent: 'center',
    alignItems: 'center'
  },
  amountPercentage: {
    color: shotgun.brandLight
  },
  toggleStage: {
    marginRight: 5,
    justifyContent: 'center',
    flex: 1
  },
  toggleStageRow: {
    marginBottom: 10,
    height: 50
  },
  heading: {
    fontWeight: 'bold',
    fontSize: 16,
    marginTop: 10,
    marginBottom: 10
  },
  subHeading: {
    fontSize: 14,
    fontWeight: 'bold',
    alignSelf: 'flex-start'
  },
  description: {
    alignSelf: 'flex-start'
  },
  waitingSpinner: {
    height: 15,
    marginTop: 5,
    marginBottom: 5,
    alignSelf: 'center'
  },
  successText: {
    color: shotgun.brandSuccess,
    alignSelf: 'center',
    fontWeight: 'bold'
  },
  checkmark: {
    paddingTop: 5,
    alignSelf: 'center',
    fontSize: 30,
    color: shotgun.brandSuccess,
  },
  stageButton: {
    marginRight: 0,
    marginLeft: 10,
  },
  smlprice: {
    fontSize: 15,
    fontWeight: 'bold'
  }
};
