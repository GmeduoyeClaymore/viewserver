import React, { Component } from 'react';
import {Picker} from 'react-native';
import {Button, Container, ListItem, Header, Text, Title, Body, Left, Grid, Row, Col, Content} from 'native-base';
import {getDaoState, getOperationError} from 'common/dao';
import {ValidatingButton, CardIcon, ErrorRegion, Icon, OriginDestinationSummary, CurrencyInput} from 'common/components';
import DatePicker from 'common/components/datePicker/DatePicker';
import moment from 'moment';
import yup from 'yup';
import shotgun from 'native-base-theme/variables/shotgun';
import * as ContentTypes from 'common/constants/ContentTypes';
import {PaymentTypes} from 'common/constants/PaymentTypes';
import {withExternalState} from 'custom-redux';

class DeliveryOptions extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isDatePickerVisible: false
    };

    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  componentDidMount() {
    if (this.props.defaultPayment){
      this.setCard(this.props.defaultPayment);
    }
  }

  setAmount = (amount) => {
    const {order} = this.props;
    this.setState({ order: {...order, amount}});
  }

  setPaymentType = (paymentType) => {
    const {order} = this.props;
    this.setState({order: {...order, paymentType}});
  }

  setCard = ({id: paymentId, brand, last4}) => {
    this.setState({ payment: { paymentId, brand, last4} });
  }

  toggleDatePicker = (isDatePickerVisible) => {
    super.setState({isDatePickerVisible});
  }

  onChangeDate = (value) => {
    const {order} = this.props;
    this.setState({ order: {...order, requiredDate: value}});
    this.toggleDatePicker(false);
  }

  render() {
    const {resources} = this;
    const {paymentCards, errors, next, order, payment = {}, history} = this.props;
    const {isDatePickerVisible} = this.state;

    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{resources.PageTitle}</Title></Body>
      </Header>
      <Content>
        <ListItem padded>
          <Row>
            <OriginDestinationSummary order={order}/>
          </Row>
        </ListItem>
        <ListItem padded onPress={() => this.toggleDatePicker(true)}>
          <Icon paddedIcon name="delivery-time" />
          {order.requiredDate !== undefined ? <Text>{moment(order.requiredDate).format('ddd Do MMMM, h:mma')}</Text> : <Text grey>{resources.JobStartCaption}</Text>}
          <DatePicker cannedDateOptions={resources.CannedStartDateOptions} asapDateResolver={resources.AsapStartDateResolver}
            isVisible={isDatePickerVisible} onCancel={() => this.toggleDatePicker(false)} onConfirm={this.onChangeDate} {...datePickerOptions} minimumDate={resources.MinimumStartTimeOptions} />
        </ListItem>

<<<<<<< HEAD
        {resources.AllowFixedPrice ?
          <ListItem padded>
            <Grid>
              <Row>
                <Text>This job will cost</Text>
              </Row>
              <Row>
                <Col>
                  <CurrencyInput onValueChange={this.setAmount}/>
                </Col>
                <Button style={styles.periodButton} light={order.paymentType !== 'DayRate'} onPress={() => this.setPaymentType('DayRate')}>
                  <Text style={styles.buttonText}>Day Rate</Text>
                </Button>
                <Button style={styles.periodButton} light={order.paymentType !== 'Fixed'} onPress={() => this.setPaymentType('Fixed')}>
                  <Text style={styles.buttonText}>Fixed Price</Text>
                </Button>
              </Row>
            </Grid>
          </ListItem>
          : null}
=======
        <ListItem padded>
          <Grid>
            <Row>
              <Text style={styles.amountLabel}>Amount you want to pay for this job</Text>
            </Row>
            <Row>
              <Col>
                <CurrencyInput onValueChange={this.setAmount}/>
              </Col>
              {resources.AllowDayRate ? <Button style={styles.periodButton} light={order.paymentType === PaymentTypes.FIXED} onPress={() => this.setPaymentType(PaymentTypes.DAYRATE)}>
                <Text style={styles.buttonText}>Day Rate</Text>
              </Button> : null }
              {resources.AllowFixedPrice ? <Button style={styles.periodButton} light={order.paymentType === PaymentTypes.DAYRATE} onPress={() => this.setPaymentType(PaymentTypes.FIXED)}>
                <Text style={styles.buttonText}>Fixed Price</Text>
              </Button> : null }
            </Row>
          </Grid>
        </ListItem>
>>>>>>> dcc2b15a201a68a40631b26470f1437cb13ed1de

        <ListItem padded style={{borderBottomWidth: 0}}>
          <CardIcon brand={payment.brand} /><Text>Use card</Text>
          <Picker style={styles.cardPicker} itemStyle={{height: 38}} selectedValue={payment.paymentId} onValueChange={(c, i) => this.setCard(paymentCards[i])}>
            {paymentCards.map((c,idx) => <Picker.Item key={idx} label={`****${c.last4}  ${c.expMonth}/${c.expYear}`} value={c.id} />)}
          </Picker>
        </ListItem>
        <Text note style={styles.noteText}>You will not be charged until the job has been completed</Text>
      </Content>
      <ErrorRegion errors={errors}/>
      <ValidatingButton fullWidth paddedBottom iconRight onPress={() => history.push(next)} validationSchema={yup.object(validationSchema)} validateOnMount={true} model={order}>
        <Text uppercase={false}>Continue</Text>
        <Icon next name='forward-arrow' />
      </ValidatingButton>
    </Container>;
  }
}

const validationSchema = {
  requiredDate: yup.date().required()
};

const datePickerOptions = {
  datePickerModeAndroid: 'calendar',
  mode: 'datetime',
  titleIOS: 'Select delivery time',
  maximumDate: moment().add(14, 'days').toDate()
};

const ASAPWorkingDateOption = {
  name: 'ASAP',
  resolver: () => moment().add(1, 'days').startOf('day').add(8, 'hours').toDate()
};

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', 'Delivery Options').
    delivery('Delivery Details').
    personell('Job Details').
    hire('Hire Duration').
    skip('Hire Duration').
    rubbish('Collection Details').
  property('JobStartCaption', 'Set a collection time').
    delivery('Set a collection time').
    personell('Job Start Date').
  property('JobEndCaption', 'Set a return time').
    delivery('Set a return time').
    personell('Job End Date').
  property('AllowFixedPrice', true).
    personell(true).
  property('AllowDayRate', false).
    personell(true).
  property('MinimumStartTimeOptions', moment().startOf('day').toDate()).
    personell(moment().add(1, 'days').toDate()).
  property('CannedStartDateOptions', undefined).
    personell([ASAPWorkingDateOption])
/*eslint-enable */

const styles = {
  cardPicker: {
    width: 200
  },
  calculatedPrice: {
    marginTop: 12,
    fontSize: 18,
    alignSelf: 'flex-start',
    fontWeight: 'bold'
  },
  calculatedPricePlaceholder: {
    color: shotgun.coolGrey,
    marginTop: 12,
    fontSize: 12,
    alignSelf: 'flex-start'
  },
  amountLabel: {
    marginBottom: 10
  },
  periodButton: {
    marginLeft: 5,
    justifyContent: 'center',
    width: 100
  },
  buttonText: {
    fontSize: 10
  },
  noteText: {
    alignSelf: 'center',
    paddingBottom: 15
  }
};

const mapStateToProps = (state, initialProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  const paymentCards = getDaoState(state, ['paymentCards'], 'paymentDao') || [];
  const defaultPayment = paymentCards.find(c => c.id == user.stripeDefaultSourceId) || paymentCards[0];

  return {
    ...initialProps,
    errors: getOperationError(state, 'paymentDao', 'getPaymentCards' ),
    defaultPayment,
    paymentCards,
    user
  };
};

export default withExternalState(mapStateToProps)(DeliveryOptions);


