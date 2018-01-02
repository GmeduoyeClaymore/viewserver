import React, {Component} from 'react';
import {Switch} from 'react-native';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Picker} from 'react-native';
import {Icon, Button, Container, Form, Label, Item, Header, Text, Title, Body, Left, Grid, Row, Col, Content} from 'native-base';
import {getDaoState} from 'common/dao';
import {merge} from 'lodash';
import { withRouter } from 'react-router';
import {isAnyOperationPending} from 'common/dao';
import LoadingScreen from 'common/components/LoadingScreen';
import DatePicker from 'common/components/datePicker/DatePicker';
import moment from 'moment';
import ValidatingButton from 'common/components/ValidatingButton';
import yup from 'yup';

class DeliveryOptions extends Component {
  constructor(props) {
    super(props);
    this.onChangeValue = this.onChangeValue.bind(this);
    this.setRequireHelp = this.setRequireHelp.bind(this);
    this.state = {
      requireHelp: false,
      isDatePickerVisible: false,
      date: undefined
    };
  }

  async componentDidMount(){
    const {stripeDefaultPaymentSource} = this.props.user;
    const defaultCard = this.props.paymentCards.find(c => c.id == stripeDefaultPaymentSource) || this.props.paymentCards[0];
    if (defaultCard){
      this.setCard(defaultCard.id);
    }
  }

  setCard(paymentId){
    this.props.context.setState({payment: {paymentId}});
  }

  toggleDatePicker(isDatePickerVisible) {
    this.setState({isDatePickerVisible});
  }

  setRequireHelp(requireHelp){
    this.setState({requireHelp});
    this.onChangeValue('noRequiredForOffload', 0);
  }

  onChangeValue(field, value) {
    const {context} = this.props;
    const {delivery} = context.state;
    context.setState({delivery: merge({}, delivery, {[field]: value})});
  }

  render() {
    const {history, context, vehicleTypes, busy, paymentCards = []} = this.props;
    const {delivery, payment} = context.state;
    const {requireHelp, isDatePickerVisible} = this.state;

    const datePickerOptions = {
      datePickerModeAndroid: 'calendar',
      mode: 'datetime',
      titleIOS: 'Select delivery time',
      minimumDate: moment().startOf('day').toDate(),
      maximumDate: moment().add(14, 'days').toDate()
    };

    return busy ? <LoadingScreen text="Loading Customer Cards"/> : <Container>
      <Header>
        <Left>
          <Button transparent>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>Delivery Instructions</Title></Body>
      </Header>
      <Content>
        <Form>
          <Text>What size van?</Text>
          <Picker selectedValue={delivery.vehicleTypeId} onValueChange={(itemValue) => this.onChangeValue('vehicleTypeId', itemValue)}>
            <Picker.Item label="--Select Vehicle Type--"/>
            {vehicleTypes.map(c => <Picker.Item  key={c.vehicleTypeId} label={c.bodyType} value={c.vehicleTypeId} />)}
          </Picker>

          <Item onPress={() => this.toggleDatePicker(true)}>
            <Grid>
              <Col>
                <Row><Text>Needed</Text></Row>
                <Row><Text>{delivery.eta !== undefined ? moment(delivery.eta).format('Do MMM') : 'Choose date'}</Text></Row>
              </Col>
              <Col>
                <Row>
                  <Text>at</Text>
                </Row>
                <Row>
                  <Text>{delivery.eta !== undefined ? moment(delivery.eta).format('HH:mm') : 'Choose time'}</Text>
                </Row>
              </Col>
            </Grid>
            <DatePicker isVisible={isDatePickerVisible} onCancel={() => this.toggleDatePicker(false)} onConfirm={(date) => this.onChangeValue('eta', date)} {...datePickerOptions}/>
          </Item>

          <Item fixedLabel>
            <Label>Do you require help with this item</Label>
            <Switch onValueChange={this.setRequireHelp} value={requireHelp}/>
          </Item>
          {requireHelp ? <Item fixedLabel>
            <Label>How many people</Label>
            <Button transparent onPress={() => this.onChangeValue('noRequiredForOffload', 1)} >
              <Text>1</Text>
              <Icon name='man' style={styles.manIcon}/>
            </Button>
            <Button transparent onPress={() => this.onChangeValue('noRequiredForOffload', 2)} >
              <Text>2</Text>
              <Icon name='man' style={styles.manIcon}/>
              <Icon name='man' style={styles.manIcon}/>
            </Button>
            <Button transparent onPress={() => this.onChangeValue('noRequiredForOffload', 3)} >
              <Text>3</Text>
              <Icon name='man' style={styles.manIcon}/>
              <Icon name='man' style={styles.manIcon}/>
              <Icon name='man' style={styles.manIcon}/>
            </Button>
          </Item> : null}

          <Text>Pay with card</Text>
          <Picker selectedValue={payment.paymentId} onValueChange={(itemValue) => this.setCard(itemValue)}>
            {paymentCards.map(c => <Picker.Item key={c.id} label={`************${c.last4}  ${c.expMonth}/${c.expYear}`} value={c.id} />)}
          </Picker>
          <ValidatingButton onPress={() =>  history.push('/Customer/Checkout/ItemDetails')} validationSchema={yup.object(validationSchema)} validateOnMount={true} model={delivery}>
            <Text>Next</Text>
          </ValidatingButton>
        </Form>
      </Content>
    </Container>;
  }
}

const validationSchema = {
  vehicleTypeId: yup.string().required(),
  eta: yup.date().required(),
};

DeliveryOptions.PropTypes = {
  paymentCards: PropTypes.array,
  user: PropTypes.object
};

const styles = {
  manIcon: {
    marginLeft: 0,
    marginRight: 0,
    paddingLeft: 0,
    paddingRight: 0
  }
};

const mapStateToProps = (state, initialProps) => ({
  busy: isAnyOperationPending(state, { paymentDao: 'getCustomerPaymentCards'}),
  paymentCards: getDaoState(state, ['paymentCards'], 'paymentDao') || [],
  vehicleTypes: getDaoState(state, ['vehicleTypes'], 'vehicleTypeDao'),
  user: getDaoState(state, ['user'], 'userDao'),
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(DeliveryOptions));


