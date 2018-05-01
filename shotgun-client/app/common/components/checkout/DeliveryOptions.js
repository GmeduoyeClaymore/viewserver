import React, { Component } from 'react';
import {Picker} from 'react-native';
import {Button, Container, ListItem, Header, Text, Title, Body, Left, Grid, Row, Col, Content} from 'native-base';
import {getDaoState, getOperationError} from 'common/dao';
import {Currency, ValidatingButton, CardIcon, ErrorRegion, Icon, OriginDestinationSummary} from 'common/components';
import DatePicker from 'common/components/datePicker/DatePicker';
import moment from 'moment';
import yup from 'yup';
import shotgun from 'native-base-theme/variables/shotgun';
import * as ContentTypes from 'common/constants/ContentTypes';
import {withExternalState} from 'custom-redux';

class DeliveryOptions extends Component {
  constructor(props) {
    super(props);
    this.state = {};
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  componentDidMount() {
    if (this.props.defaultPayment){
      this.setCard(this.props.defaultPayment);
    }
  }

  setCard = ({id: paymentId, brand, last4}) => {
    this.setState({ payment: { paymentId, brand, last4} });
  }

  toggleDatePicker = (isDatePickerVisible) => {
    super.setState({isDatePickerVisible});
  }

  onChangeDate = (value) => {
    const {order} = this.props;
    this.setState({ order: {...order, requiredDate: value}}, this.calculateEndTime);
    this.toggleDatePicker(false);
  }

  render() {
    const {resources} = this;
    const {paymentCards, errors, next, order, payment = {}, selectedContentType, history} = this.props;
    const {isDatePickerVisible} = this.state;

    const validationSchema = {};

    validationSchema.requiredDate = yup.date().required();
    validationSchema.amount = yup.number().required();

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
            <OriginDestinationSummary order={order} contentType={selectedContentType}/>
          </Row>
        </ListItem>
        <ListItem padded onPress={() => this.toggleDatePicker(true)}>
          <Icon paddedIcon name="delivery-time" />
          {order.requiredDate !== undefined ? <Text>{moment(order.requiredDate).format('ddd Do MMMM, h:mma')}</Text> : <Text grey>{resources.StartTime}</Text>}
          <DatePicker cannedDateOptions={resources.CannedStartDateOptions} asapDateResolver={resources.AsapStartDateResolver}
            isVisible={isDatePickerVisible} onCancel={() => this.toggleDatePicker(false)} onConfirm={this.onChangeDate} {...datePickerOptions} minimumDate={resources.MinimumStartTimeOptions} />
        </ListItem>
        <ListItem padded>
          <Grid>
            <Row>
              <Text>This job will cost</Text>
            </Row>
            <Row>
              <Col>
                <Currency value={order.amount} style={styles.calculatedPrice}/>
              </Col>
            </Row>
          </Grid>
        </ListItem>

        <ListItem padded style={{borderBottomWidth: 0}}>
          <CardIcon brand={payment.brand} /><Text>Use card</Text>
          <Picker style={styles.cardPicker} itemStyle={{height: 38}} selectedValue={payment.paymentId} onValueChange={(c, i) => this.setCard(paymentCards[i])}>
            {paymentCards.map(c => <Picker.Item key={c.id} label={`****${c.last4}  ${c.expMonth}/${c.expYear}`} value={c.id} />)}
          </Picker>
        </ListItem>
        <Text note style={styles.noteText}>You will not be charged until the job has been completed</Text>
        <ErrorRegion errors={errors}/>
        <ValidatingButton fullWidth paddedBottom iconRight onPress={() => history.push(next)} validationSchema={yup.object(validationSchema)} validateOnMount={true} model={order}>
          <Text uppercase={false}>Continue</Text>
          <Icon next name='forward-arrow' />
        </ValidatingButton>
      </Content>
    </Container>;
  }
}

const datePickerOptions = {
  datePickerModeAndroid: 'calendar',
  mode: 'datetime',
  titleIOS: 'Select delivery time',
  maximumDate: moment().add(14, 'days').toDate()
};

const TommorowDateOption = {
  name: 'Tmrw',
  resolver: () => moment().add(1, 'days').startOf('day').add(9, 'hours').minute(0).toDate()
};

const ASAPWorkingDateOption = {
  name: 'ASAP',
  resolver: () => moment().add(1, 'days').startOf('day').add(8, 'hours').toDate()
};

const OneWorkingDayOption = {
  name: '1 Day',
  resolver: ({from}) => (moment(from) || moment()).startOf('day').add(9, 'hours').add(8, 'hours').toDate()
};

const TwoWorkingDayOption = {
  name: '2 Days',
  resolver: ({from}) => (moment(from) || moment()).startOf('day').add(1, 'days').add(9, 'hours').add(8, 'hours').toDate()
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
  property('NoPeopleCaption', 'How Many People Do you need?').
    delivery('Do you need more than one person to lift your item?').
    personell('Does this job require additional labourers?').
  property('JobStartCaption', 'Set a collection time').
    delivery('Set a collection time').
    personell('Job Start Date').
  property('JobEndCaption', 'Set a return time').
    delivery('Set a return time').
    personell('Job End Date').
  property('AllowFixedPrice', false).
    personell(true).
  property('CannedStartDateOptions', undefined).
    personell([ASAPWorkingDateOption, TommorowDateOption]).
    property('CannedEndDateOptions', undefined).
    personell([OneWorkingDayOption, TwoWorkingDayOption]).
  property('supportsFromTime', true).
  property('supportsTillTime', true).
    rubbish(false).
  property('supportsOrigin', true).
  property('supportsDestination', true).
      skip(false).
      hire(false).
      rubbish(false).
  property('supportsNoPeople', true).
      rubbish(false).
      hire(false);
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
  periodButton: {
    marginLeft: 5,
    justifyContent: 'center',
    width: 100
  },
  buttonText: {
    fontSize: 10
  },
  fixedPriceInput: {
    borderBottomWidth: 0,
    paddingLeft: 0,
    fontSize: 18,
    fontWeight: 'bold',
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
  const {payment} = initialProps;
  return {
    ...initialProps,
    errors: getOperationError(state, 'paymentDao', 'getCustomerPaymentCards' ),
    defaultPayment,
    payment: payment || defaultPayment,
    paymentCards,
    user
  };
};

export default withExternalState(mapStateToProps)(DeliveryOptions);


