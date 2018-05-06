import React, { Component } from 'react';
import { Text, Row, Col, Spinner, View } from 'native-base';
import { SpinnerButton, Currency, Icon } from 'common/components';
import { completePaymentStage, startPaymentStage } from 'partner/actions/PartnerActions';
import shotgun from 'native-base-theme/variables/shotgun';

const CAN_START_PAYMENT_STAGE_STATUS = ['None'];
const CAN_COMPLETE_PAYMENT_STAGE_STATUS = ['Started'];

const StageQuantity = ({ quantity, paymentStageType, orderAmount }) => {
  const isPercent = paymentStageType === 'Percentage';
  const currencyTotal = isPercent ? orderAmount * (quantity / 100) : quantity;
  return <Currency value={currencyTotal} style={styles.smlprice} suffix={isPercent ? ` (${quantity}%)` : ''} />;
};

const CompletePaymentStage = ({ orderId, paymentStageId, onPaymentStageRemoved, orderContentTypeId, busyUpdating, dispatch, ...rest }) => {
  const onUpdateOrderAmount = () => {
    dispatch(completePaymentStage({ orderId, paymentStageId, orderContentTypeId }, onPaymentStageRemoved));
  };
  return <SpinnerButton {...rest} padded busy={busyUpdating} style={{ alignSelf: 'flex-start', flex: 1, marginRight: 0, marginLeft: 0 }} fullWidth success onPress={onUpdateOrderAmount}><Text uppercase={false}>Complete</Text></SpinnerButton>;
};

const StartPaymentStage = ({ orderId, paymentStageId, onPaymentStageRemoved, orderContentTypeId, busyUpdating, dispatch, ...rest }) => {
  const onUpdateOrderAmount = () => {
    dispatch(startPaymentStage({ orderId, paymentStageId, orderContentTypeId }, onPaymentStageRemoved));
  };
  return <SpinnerButton {...rest} padded busy={busyUpdating} style={{ alignSelf: 'flex-start', flex: 1, marginRight: 0, marginLeft: 0 }} success fullWidth onPress={onUpdateOrderAmount}><Text uppercase={false}>Start</Text></SpinnerButton>;
};

const PartnerPaymentStagesControl = ({ paymentStages = [], orderId, orderStatus, orderContentTypeId, paymentStageType, busyUpdating, dispatch, orderAmount, negotiatedResponseStatus }) => {
  const hasStartedStage = paymentStages.find(c => c.paymentStageStatus === 'Started');
  return <Col>{paymentStages.map(
    (paymentStage, idx) => {
      const { quantity, name, description, paymentStageType, paymentStageStatus, id, lastUpdated } = paymentStage;
      return <Row key={idx} style={{ marginBottom: 10, marginLeft: 3, width: '100%', flex: -1 }}>
        <Col size={50}>
          <Text style={{ ...styles.subHeading, marginBottom: 5 }}>{name}</Text>
          <Text>{description}</Text>
        </Col>
        <Col size={15} style={{ paddingTop: 10 }}>
          <StageQuantity quantity={quantity} paymentStageType={paymentStageType} orderAmount={orderAmount} />
        </Col>
        <Col size={15} >
          {!!~CAN_START_PAYMENT_STAGE_STATUS.indexOf(paymentStageStatus) && negotiatedResponseStatus === 'ASSIGNED' && !hasStartedStage ? <StartPaymentStage busyUpdating={busyUpdating} style={{ marginBottom: 10 }} orderId={orderId} orderContentTypeId={orderContentTypeId} paymentStageId={id} dispatch={dispatch} /> : null}
          {!!~CAN_COMPLETE_PAYMENT_STAGE_STATUS.indexOf(paymentStageStatus) && negotiatedResponseStatus === 'ASSIGNED' ? <CompletePaymentStage busyUpdating={busyUpdating} orderId={orderId} orderContentTypeId={orderContentTypeId} paymentStageId={id} dispatch={dispatch} /> : null}
          {paymentStageStatus === 'Complete' ? <Spinner style={{ height: 50, paddingRight: 15 }} /> : null}
          {paymentStageStatus === 'Paid' ? <Icon name='star' style={styles.star} /> : null}
        </Col>
      </Row>;
    })}
  </Col>;
};

export default class OrderPaymentStagePanel extends Component {
  constructor(props) {
    super(props);
    this.state = {
      paymentStageType: 'Percentage'
    };
  }

  setPaymentStageType = (paymentStageType) => {
    this.setState({ paymentStageType });
  }

  render() {
    const { order, busyUpdating, dispatch, negotiatedResponseStatus } = this.props;
    const { paymentStages = [] } = order;
    if (!order || !paymentStages.length) {
      return null;
    }
    return <View style={{ paddingLeft: 15, paddingRight: 15, flex: 1 }}>
      < Text style={{ ...styles.heading, marginTop: 10, marginBottom: 10 }}>Payment Stages</Text>
      <PartnerPaymentStagesControl
        paymentStageType={this.state.paymentStageType} negotiatedResponseStatus={negotiatedResponseStatus} dispatch={dispatch} orderId={order.orderId} orderStatus={order.orderStatus} orderAmount={order.amount} orderContentTypeId={order.orderContentTypeId} paymentStages={paymentStages} busyUpdating={busyUpdating} />
    </View>;
  }
}

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
    marginLeft: 30,
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    marginTop: 5
  },
  heading: {
    fontWeight: 'bold',
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
    paddingTop: 10,
    alignSelf: 'center',
    fontSize: 30,
    padding: 2,
    color: shotgun.brandSuccess,
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
