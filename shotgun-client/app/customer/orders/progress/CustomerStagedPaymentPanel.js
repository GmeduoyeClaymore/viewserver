import React, {Component} from 'react';
import {Button, Text, Grid, Row, Col,  Item, Label, Spinner, View} from 'native-base';
import {SpinnerButton, Currency, ValidatingInput, ValidatingButton, Icon} from 'common/components';
import * as ContentTypes from 'common/constants/ContentTypes';
import {addPaymentStage, removePaymentStage, payForPaymentStage} from 'customer/actions/CustomerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';

const CAN_ADD_PAYMENT_STAGE__ORDER_STATUSES = ['PLACED', 'ACCEPTED', 'INPROGRESS'];
const CAN_MODIFY_PAYMENT_STAGE_STATUS = ['None'];
const CAN_PAY_PAYMENT_STAGE_STATUS = ['Complete'];

const validationSchemaBase = {
  name: yup.string().required().max(30),
  description: yup.string().required().max(150),
};

const percentageValidationSchema = () => ({
  ...validationSchemaBase,
  amount: yup.number().required().min(0).max(100)
});

const fixedPriceValidationSchema = orderAmount => ({
  ...validationSchemaBase,
  amount: yup.number().required().min(0).max(orderAmount)
});

const StageQuantity = ({quantity, paymentStageType, orderAmount}) => {
  const isPercent = paymentStageType === 'Percentage';
  const currencyTotal = isPercent ? orderAmount * (quantity / 100) : quantity;
  return <Currency value={currencyTotal} style={styles.smlprice} suffix={isPercent ? ` (${quantity}%)` : ''}/>;
};

const  AddPaymentStage = (props) => {
  const {orderId, orderContentTypeId, amount, orderAmount, name, description, paymentStageType, onPaymentStageAdded, busyUpdating, dispatch, ...rest} = props;
  const validationSchemaFactory = paymentStageType === 'Percentage' ? percentageValidationSchema : fixedPriceValidationSchema;
  const validationSchema = validationSchemaFactory(orderAmount);
  const onAddPaymentStage = () => {
    dispatch(addPaymentStage({orderId, orderContentTypeId, amount, name, description, paymentStageType}, onPaymentStageAdded));
  };
  return <ValidatingButton {...rest} busy={busyUpdating} fullWidth success onPress={onAddPaymentStage} validationSchema={yup.object(validationSchema)} model={props}>
    <Text uppercase={false}>Add</Text>
  </ValidatingButton>;
};

const  PayForStage = ({orderId, paymentStageId, onPaymentStageRemoved, orderContentTypeId, busyUpdating, dispatch, ...rest}) => {
  const onUpdateOrderAmount = () => {
    dispatch(payForPaymentStage({orderId, paymentStageId, orderContentTypeId}, onPaymentStageRemoved));
  };
  return <SpinnerButton {...rest} padded busy={busyUpdating} style={{alignSelf: 'flex-start', flex: 1, marginRight: 0, marginLeft: 0}} fullWidth success onPress={onUpdateOrderAmount}><Text uppercase={false}>Pay</Text></SpinnerButton>;
};

const  RemovePaymentStage = ({orderId, paymentStageId, onPaymentStageRemoved, orderContentTypeId, busyUpdating, dispatch, ...rest}) => {
  const onUpdateOrderAmount = () => {
    dispatch(removePaymentStage({orderId, paymentStageId, orderContentTypeId}, onPaymentStageRemoved));
  };
  return <SpinnerButton {...rest} padded busy={busyUpdating} style={{alignSelf: 'flex-start', flex: 1, marginRight: 0, marginLeft: 0}} danger fullWidth onPress={onUpdateOrderAmount}><Text uppercase={false}>Del</Text></SpinnerButton>;
};

class AddPaymentStageControl extends Component{
  constructor(props){
    super(props);
    this.state = {
      amount: undefined,
      name: undefined,
      description: undefined
    };
  }

  resetState = () => {
    this.setState({
      amount: undefined,
      name: undefined,
      description: undefined,
      reset: true
    });
    if (this.nameInput){
      this.nameInput.clearTouched();
    }
    if (this.descriptionInput){
      this.descriptionInput.clearTouched();
    }
    if (this.amountInput){
      this.amountInput.clearTouched();
    }
  }

  render(){
    const {paymentStageType, orderAmount} = this.props;
    const validationSchemaFactory = paymentStageType === 'Percentage' ? percentageValidationSchema : fixedPriceValidationSchema;
    const validationSchema = validationSchemaFactory(orderAmount);
    return <Col style={{maxHeight: 280, minHeight: 280}}>
      <Item key="1" stackedLabel  style={{marginLeft: 4, marginRight: 10, flex: 1, marginBottom: 10}} >
        <Label>Name</Label>
        <ValidatingInput ref={rf => {this.nameInput = rf;}} bold style={{padding: 10}} padded value={this.state.name}placeholder="First fix electrics" onChangeText={(name) => this.setState({name})} validationSchema={validationSchema.name} maxLength={30}/>
      </Item>
      <Item key="2" stackedLabel  style={{marginLeft: 4, marginRight: 10, flex: 1, marginBottom: 10}} >
        <Label>Description</Label>
        <ValidatingInput ref={rf => {this.descriptionInput = rf;}} bold style={{padding: 10}} padded value={this.state.description} placeholder="Complete all first fix of bathroom" onChangeText={(description) => this.setState({description})} validationSchema={validationSchema.name} maxLength={80}/>
      </Item>
      <Item key="3" stackedLabel  style={{marginLeft: 4, marginRight: 10, flex: 1, marginBottom: 10}} >
        <Label>{paymentStageType === 'Percentage' ?  'Percentage' : 'Amount'}</Label>
        <ValidatingInput ref={rf => {this.amountInput = rf;}} bold style={{padding: 10}} value={this.state.amount} placeholder="25" onChangeText={(amount) => this.setState({amount})} validationSchema={validationSchema.amount} maxLength={10}/>
      </Item>
      <AddPaymentStage {...this.props} {...this.state} onPaymentStageAdded={this.resetState}/>
    </Col>;
  }
}

const PartnerPaymentStagesControl = ({canAddPaymentStages, paymentStages = [], orderId, orderStatus, orderContentTypeId, paymentStageType, busyUpdating, dispatch, orderAmount}) => {
  return <Col>{paymentStages.map(
    (paymentStage, idx)  => {
      const {quantity, name, description, paymentStageType, paymentStageStatus, id, lastUpdated} = paymentStage;
      return <Row key={idx} style={{marginBottom: 10, marginLeft: 3, width: '100%', flex: -1}}>
        <Col  size={50}>
          <Text style={{...styles.subHeading, marginBottom: 5}}>{name}</Text>
          <Text>{description}</Text>
        </Col>
        <Col size={15} style={{paddingTop: 10}}>
          <StageQuantity quantity={quantity} paymentStageType={paymentStageType} orderAmount={orderAmount}/>
        </Col>
        <Col size={15} >
          {!!~CAN_MODIFY_PAYMENT_STAGE_STATUS.indexOf(paymentStageStatus) && !!~CAN_ADD_PAYMENT_STAGE__ORDER_STATUSES.indexOf(orderStatus) ? <RemovePaymentStage busyUpdating={busyUpdating} style={{marginBottom: 10}} orderId={orderId} orderContentTypeId={orderContentTypeId} paymentStageId={id} dispatch={dispatch}/> : null}
          {!!~CAN_PAY_PAYMENT_STAGE_STATUS.indexOf(paymentStageStatus) ? <PayForStage busyUpdating={busyUpdating} orderId={orderId} orderContentTypeId={orderContentTypeId} paymentStageId={id} dispatch={dispatch}/> : null}
          {paymentStageStatus === 'Started' ?  <Spinner style={{height: 50, paddingRight: 15}}/> : null}
          {paymentStageStatus === 'Paid' ? <Icon name='star' style={styles.star}/> : null}
        </Col>
      </Row>;
    })}
  {canAddPaymentStages ? <AddPaymentStageControl orderAmount={orderAmount} busyUpdating={busyUpdating} paymentStageType={paymentStageType} orderId={orderId} orderContentTypeId={orderContentTypeId} dispatch={dispatch}/> : null}
  </Col>;
};

export default class OrderPaymentStagePanel extends Component{
  constructor(props) {
    super(props);
    this.state = {
      paymentStageType: 'Percentage'
    };
  }

  setPaymentStageType = (paymentStageType) => {
    this.setState({paymentStageType});
  }

  render(){
    const {order, busyUpdating, dispatch, height} = this.props;
    const {paymentStages = []} = order;
    const canAddPaymentStages = !order.blockPaymentStageAddition && order.paymentType !== 'DAYRATE';
    if (!order){
      return null;
    }
    return <View style={{paddingLeft: 15, paddingRight: 15, flex: 1}}>
      <Row  key="1" style={{paddingTop: 10, marginBottom: 10, flex: -1}}>
        <Col  key="2" size={32}>
          < Text style={{...styles.heading, marginTop: 10, marginBottom: 10}}>{canAddPaymentStages ? 'Add Payment Stages' : 'Payment Stages'} </Text>
          
        </Col>
      </Row>
      <Col style={{flex: 10}}>
        {canAddPaymentStages && !paymentStages.length ?
          <Row style={{flex: -1, marginBottom: 10}}>
            <Button style={styles.toggleStage} light={this.state.paymentStageType === 'Fixed'} onPress={() => this.setPaymentStageType('Percentage')}>
              <Text style={styles.buttonText}>Percentage Stages</Text>
            </Button>
            <Button style={styles.toggleStage} light={this.state.paymentStageType  === 'Percentage'} onPress={() => this.setPaymentStageType('Fixed')}>
              <Text style={styles.buttonText}>Fixed Stages</Text>
            </Button>
          </Row> : null}
        <PartnerPaymentStagesControl key="3"
          paymentStageType={this.state.paymentStageType} canAddPaymentStages={canAddPaymentStages} dispatch={dispatch} orderId={order.orderId} orderStatus={order.orderStatus} orderAmount={order.amount} orderContentTypeId={order.orderContentTypeId} paymentStages={paymentStages} busyUpdating={busyUpdating}/>
      </Col>
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
