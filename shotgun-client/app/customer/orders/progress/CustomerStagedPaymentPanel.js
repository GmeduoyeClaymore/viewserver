import React, {Component} from 'react';
import {View, Button, Text, Row, Col, Spinner, ListItem} from 'native-base';
import {SpinnerButton, Currency, Icon} from 'common/components';
import {removePaymentStage, payForPaymentStage} from 'customer/actions/CustomerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import AddPaymentStageControl from './AddPaymentStageControl';
import {Platform} from 'react-native';
const IS_ANDROID = Platform.OS === 'android';
const CAN_ADD_PAYMENT_STAGE__ORDER_STATUSES = ['PLACED', 'ACCEPTED', 'INPROGRESS'];
const CAN_MODIFY_PAYMENT_STAGE_STATUS = ['None'];
const CAN_PAY_PAYMENT_STAGE_STATUS = ['Complete'];

export default class CustomerStagedPaymentPanel extends Component{
  constructor(props) {
    super(props);
    this.state = {
      paymentStageType: 'Percentage'
    };
  }

  setPaymentStageType = (paymentStageType) => {
    this.setState({paymentStageType});
  }

  onPayForPaymentStage = (paymentStageId) => {
    const {dispatch, order} = this.props;
    const {orderId, orderContentTypeId} = order;

    dispatch(payForPaymentStage({orderId, paymentStageId, orderContentTypeId}));
  };

  onRemovePaymentStage = (paymentStageId) => {
    const {dispatch, order} = this.props;
    const {orderId, orderContentTypeId} = order;
    dispatch(removePaymentStage({orderId, paymentStageId, orderContentTypeId}));
  };

  getPaymentStages = () => {
    const {order, busyUpdating} = this.props;
    const {paymentStages = [], orderStatus, amount} = order;
    
    return paymentStages.map(
      (paymentStage, idx)  => {
        const {quantity, name, description, paymentStageType, paymentStageStatus, id} = paymentStage;
        const isPercent = paymentStageType === 'Percentage';
        const currencyTotal = isPercent ? amount * (quantity / 100) : quantity;

        return <ListItem key={idx} style={styles.paymentStageRow}>
          <Row>
            <Col size={50}>
              <Text style={styles.subHeading}>{name}</Text>
              <Text style={styles.description}>{description}</Text>
            </Col>
            <Col size={20} style={styles.amountColumn}>
              <Currency value={currencyTotal} style={styles.smlprice}/>
              {isPercent ? <Text style={styles.amountPercentage}>{` (${quantity}%)`}</Text> : null}
            </Col>
            <Col size={30}>
              {!!~CAN_MODIFY_PAYMENT_STAGE_STATUS.indexOf(paymentStageStatus) && !!~CAN_ADD_PAYMENT_STAGE__ORDER_STATUSES.indexOf(orderStatus) ?
                <SpinnerButton busy={busyUpdating} style={[styles.stageButton]} danger fullWidth onPress={() => this.onRemovePaymentStage(id)}>
                  <Text uppercase={false}>Delete</Text>
                </SpinnerButton> :
                null}

              {!!~CAN_PAY_PAYMENT_STAGE_STATUS.indexOf(paymentStageStatus) ?
                <SpinnerButton busy={busyUpdating} style={styles.stageButton} padded fullWidth success onPress={() => this.onPayForPaymentStage(id)}>
                  <Text uppercase={false}>Pay</Text>
                </SpinnerButton> :
                null}

              {paymentStageStatus === 'Started' ?  [<Spinner  size={ IS_ANDROID ? 25 : 1} key='spinner' color={shotgun.brandSuccess} style={styles.waitingSpinner}/>, <Text key='text' style={styles.successText}>In Progress</Text>] : null}
              {paymentStageStatus === 'Paid' ? [<Icon  name='checkmark' style={styles.checkmark}/>, <Text key='text2' style={styles.successText}>Paid</Text>] : null}
            </Col>
          </Row>
        </ListItem>;
      });
  };

  getPaymentStageType = () => {
    const {order} = this.props;
    const {paymentStages = []} = order;
    if (paymentStages.length){
      return paymentStages[0].paymentStageType;
    }
    return this.state.paymentStageType;
  }

  render(){
    const {order, busyUpdating, dispatch} = this.props;
    const {paymentStages = []} = order;
    const canAddPaymentStages = !order.blockPaymentStageAddition && order.paymentType !== 'DAYRATE';

    if (!order){
      return null;
    }

    return <View padded>
      <Text style={styles.heading}>{canAddPaymentStages ? 'Add Payment Stages' : 'Payment Stages'} </Text>
      {canAddPaymentStages && !paymentStages.length ?
        <Row style={styles.toggleStageRow}>
          <Button style={styles.toggleStage} light={this.state.paymentStageType === 'Fixed'} onPress={() => this.setPaymentStageType('Percentage')}>
            <Text style={styles.buttonText} uppercase={false}>% Stages</Text>
          </Button>
          <Button style={styles.toggleStage} light={this.state.paymentStageType  === 'Percentage'} onPress={() => this.setPaymentStageType('Fixed')}>
            <Text style={styles.buttonText} uppercase={false}>Fixed Stages</Text>
          </Button>
        </Row> : null}

      {this.getPaymentStages()}
      {canAddPaymentStages ? <AddPaymentStageControl order={order} busyUpdating={busyUpdating} paymentStageType={this.getPaymentStageType()} dispatch={dispatch}/> : null}
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
  buttonText: {
    fontSize: 12
  },
  waitingSpinner: {
    height: 15,
    marginBottom: 5,
    marginTop: 5,
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
    marginLeft: 10
  },
  smlprice: {
    fontSize: 15,
    fontWeight: 'bold'
  }
};
