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
import {calculateTotalPrice} from './CheckoutUtils';
import {TextInputMask} from 'react-native-masked-text';

class DeliveryOptions extends Component {
  constructor(props) {
    super(props);

    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);

    this.state = {
      isDatePickerVisible: false,
      isFixedPrice: false,
      isDurationDays: true,
      duration: undefined,
      calculatedPrice: undefined,
      fixedPriceMask: undefined
    };
  }

  componentDidMount() {
    this.setCard(this.props.defaultPayment);
  }

  setCard = ({id: paymentId, brand, last4}) => {
    this.setState({ payment: { paymentId, brand, last4} });
  }

  toggleDatePicker = (isDatePickerVisible) => {
    super.setState({isDatePickerVisible});
  }

  setFixedPrice = (fixedPriceMask) => {
    const {orderItem} = this.props;
    const fixedPrice = this.refs.fixedPriceInput.getRawValue() * 100;
    console.log(fixedPriceMask);
    super.setState({fixedPriceMask: (fixedPrice != 0 ? fixedPriceMask : undefined)});
    this.setState({ orderItem: {...orderItem, fixedPrice}});
  }

  setIsDurationDays = (isDurationDays) => {
    super.setState({isDurationDays}, this.calculateEndTime);
  }

  onChangeDuration = (duration) => {
    super.setState({duration}, this.calculateEndTime);
  }

  toggleFixedPrice = (isFixedPrice) => {
    const {orderItem} = this.props;

    if (!isFixedPrice) {
      this.setState({orderItem: {...orderItem, fixedPrice: undefined}});
      super.setState({fixedPriceMask: undefined});
    }
    super.setState({isFixedPrice});
  }

  onChangeDate = (value) => {
    const {orderItem} = this.props;
    this.setState({ orderItem: {...orderItem, startTime: value}}, this.calculateEndTime);
    this.toggleDatePicker(false);
  }

  calculateEndTime = () => {
    const {isDurationDays, duration} = this.state;
    const {orderItem} = this.props;
    const {startTime} = orderItem;

    //TODO - validation so the duration can only be a sensible number

    if (startTime !== undefined && duration != undefined && duration.trim() !== ''){
      const momentStartTime = moment(startTime);
      const endTime = (isDurationDays ? momentStartTime.startOf('day').add(duration, 'days') : momentStartTime.add(duration, 'hours')).toDate();
      this.setState({ orderItem: {...orderItem, endTime}}, this.loadEstimatedPrice);
    }
  }

  loadEstimatedPrice = async() => {
    const {client, orderItem, delivery} = this.props;
    if (orderItem.startTime && orderItem.endTime){
      const calculatedPrice = await calculateTotalPrice({client, delivery, orderItem});
      if (calculatedPrice){
        super.setState({calculatedPrice});
      }
    }
  }

  render() {
    const {resources} = this;
    const {paymentCards, orderItem, errors, next, delivery, payment, selectedContentType, history} = this.props;
    const {isDatePickerVisible, isFixedPrice, isDurationDays, calculatedPrice, fixedPriceMask, duration} = this.state;

    const validationSchema = {};

    if (selectedContentType.hasStartTime){
      validationSchema.startTime = yup.date().required();
    }

    if (selectedContentType.hasEndTime){
      validationSchema.endTime = yup.date().required();
    }

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
            <OriginDestinationSummary contentType={selectedContentType} delivery={delivery}/>
          </Row>
        </ListItem>

        {selectedContentType.hasStartTime ?  <ListItem padded onPress={() => this.toggleDatePicker(true)}>
          <Icon paddedIcon name="delivery-time" />
          {orderItem.startTime !== undefined ? <Text>{moment(orderItem.startTime).format('ddd Do MMMM, h:mma')}</Text> : <Text grey>{resources.StartTime}</Text>}
          <DatePicker cannedDateOptions={resources.CannedStartDateOptions} asapDateResolver={resources.AsapStartDateResolver}
            isVisible={isDatePickerVisible} onCancel={() => this.toggleDatePicker(false)} onConfirm={this.onChangeDate} {...datePickerOptions} minimumDate={resources.MinimumStartTimeOptions} />
        </ListItem> : null}

        {selectedContentType.hasEndTime ?  <ListItem padded>
          <Icon paddedIcon name="delivery-time" />
          <Col>
            <TextInputMask type={'only-numbers'} keyboardType='phone-pad' underlineColorAndroid='transparent'
              placeholderTextColor={shotgun.coolGrey} placeholder={resources.EndTime} value={duration} onChangeText={this.onChangeDuration}/>
          </Col>

          <Button style={styles.periodButton} light={isDurationDays} onPress={() => this.setIsDurationDays(false)}>
            <Text style={styles.buttonText}>Hours</Text>
          </Button>
          <Button style={styles.periodButton} light={!isDurationDays} onPress={() => this.setIsDurationDays(true)}>
            <Text style={styles.buttonText}>Days</Text>
          </Button>
        </ListItem> : null}

        {resources.AllowFixedPrice ?
          <ListItem padded>
            <Grid>
              <Row>
                <Text>This job will cost</Text>
              </Row>
              <Row>
                <Col>
                  {isFixedPrice ?
                    <TextInputMask ref={'fixedPriceInput'} underlineColorAndroid='transparent' style={styles.fixedPriceInput} type={'money'} placeholder='Enter price'
                      options={{ unit: '£', separator: '.', delimiter: ','}} value={fixedPriceMask} onChangeText={this.setFixedPrice}/> :
                    calculatedPrice ? <Currency value={calculatedPrice} style={styles.calculatedPrice}/> : <Text style={styles.calculatedPricePlaceholder}>Start & duration required</Text>}
                </Col>

                <Button style={styles.periodButton} light={isFixedPrice} onPress={() => this.toggleFixedPrice(false)}>
                  <Text style={styles.buttonText}>Calculated</Text>
                </Button>
                <Button style={styles.periodButton} light={!isFixedPrice} onPress={() => this.toggleFixedPrice(true)}>
                  <Text style={styles.buttonText}>Fixed Price</Text>
                </Button>
              </Row>

            </Grid>
          </ListItem>
          : null}

        <ListItem padded style={{borderBottomWidth: 0}}>
          <CardIcon brand={payment.brand} /><Text>Use card</Text>
          <Picker style={styles.cardPicker} itemStyle={{height: 38}} selectedValue={payment.paymentId} onValueChange={(c, i) => this.setCard(paymentCards[i])}>
            {paymentCards.map(c => <Picker.Item key={c.id} label={`****${c.last4}  ${c.expMonth}/${c.expYear}`} value={c.id} />)}
          </Picker>
        </ListItem>
        <Text note style={styles.noteText}>You will not be charged until the job has been completed</Text>
        <ErrorRegion errors={errors}/>
        <ValidatingButton fullWidth paddedBottom iconRight onPress={() => history.push(next)} validationSchema={delivery.isFixedPrice ? yup.object(fixedPriceValidationSchema) : yup.object(validationSchema)} validateOnMount={true} model={orderItem}>
          <Text uppercase={false}>Continue</Text>
          <Icon next name='forward-arrow' />
        </ValidatingButton>
      </Content>
    </Container>;
  }
}

const fixedPriceValidationSchema = {
  fixedPriceValue: yup.number().required()
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
rubbish('Collection Details').
property('StartTime', 'Set a collection time').
delivery('Set a collection time').
personell('Enter job start date').
property('EndTime', 'Enter duration').
personell('Enter job duration').
property('AllowFixedPrice', false).
personell(true).
property('MinimumStartTimeOptions', moment().startOf('day').toDate()).
personell(moment().add(1, 'days').toDate()).
property('CannedStartDateOptions', undefined).
personell([ASAPWorkingDateOption]);
/*eslint-enable */

const styles = {
  cardPicker: {
    width: 200
  },
  calculatedPrice: {
    marginTop: 12,
    fontSize: 18,
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

  return {
    ...initialProps,
    errors: getOperationError(state, 'paymentDao', 'getPaymentCards' ),
    defaultPayment,
    paymentCards,
    user
  };
};

export default withExternalState(mapStateToProps)(DeliveryOptions);


