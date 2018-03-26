import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'custom-redux';
import {CheckBox, CurrencyInput, formatPrice} from 'common/components/basic';
import { Picker, TextInput } from 'react-native';
import {Button, Container, ListItem, Header, Text, Title, Body, Left, Grid, Row, Col, Content, View } from 'native-base';
import { withRouter } from 'react-router';
import {getDaoState, isAnyOperationPending, getOperationError, getNavigationProps} from 'common/dao';
import {LoadingScreen, ValidatingButton, CardIcon, ErrorRegion, Icon, OriginDestinationSummary} from 'common/components';
import DatePicker from 'common/components/datePicker/DatePicker';
import moment from 'moment';
import yup from 'yup';
import * as ContentTypes from 'common/constants/ContentTypes';
import {getPaymentCardsIfNotAlreadySucceeded} from 'customer/actions/CustomerActions';
import {calculateTotalPrice} from './CheckoutUtils';


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
    personell([OneWorkingDayOption, TwoWorkingDayOption]);
/*eslint-enable */

class DeliveryOptions extends Component {
  constructor(props) {
    super(props);
    this.onChangeValue = this.onChangeValue.bind(this);
    this.setRequireHelp = this.setRequireHelp.bind(this);
    this.loadEstimatedPrice = this.loadEstimatedPrice.bind(this);

    this.setCard = this.setCard.bind(this);
    this.toggleFixedPrice = this.toggleFixedPrice.bind(this);
    this.onFixedPriceValueChanged = this.onFixedPriceValueChanged.bind(this);

    this.state = {
      requireHelp: false,
      from_isDatePickerVisible: false,
      till_isDatePickerVisible: false,
      selectedCard: undefined,
      date: undefined
    };

    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);
  }

  async componentDidMount() {
    const {getPaymentCardsIfNotAlreadyGot} = this.props;
    if (getPaymentCardsIfNotAlreadyGot){
      getPaymentCardsIfNotAlreadyGot();
    }
    if (this.props.defaultCard !== undefined) {
      this.setCard(this.props.defaultCard);
    }
    await this.loadEstimatedPrice();
  }

  componentWillReceiveProps(nextProps) {
    ContentTypes.resolveResourceFromProps(nextProps, resourceDictionary, this);
    if (nextProps.defaultCard !== this.props.defaultCard && nextProps.defaultCard !== undefined) {
      this.setCard(nextProps.defaultCard);
    }
  }

  setCard(selectedCard) {
    this.props.context.setState({ payment: { paymentId: selectedCard.id } });
    this.setState({ selectedCard });
  }

  toggleDatePicker(pickerName, isDatePickerVisible) {
    this.setState({[pickerName + '_isDatePickerVisible']: isDatePickerVisible});
  }

  setRequireHelp(requireHelp) {
    this.setState({ requireHelp });
    this.onChangeNoItems(0);
  }

  onChangeValue(field, value, continueWith) {
    const { context } = this.props;
    const { delivery } = context.state;
    context.setState({ delivery: {...delivery, ...{ [field]: value}}}, continueWith);
  }

  onFixedPriceValueChanged(price) {
    this.onChangeValue('fixedPriceValue', price );
  }

  toggleFixedPrice(){
    const { context } = this.props;
    const { delivery } = context.state;
    const {isFixedPrice, fixedPriceValue} = delivery;
    context.setState({ delivery: {...delivery, ...{ isFixedPrice: !isFixedPrice, fixedPriceValue: isFixedPrice ? undefined : fixedPriceValue  } }});
  }

  onChangeDate(field, value) {
    this.onChangeValue(field, value, () => this.loadEstimatedPrice());
    this.toggleDatePicker(field, false);
  }

  async loadEstimatedPrice(){
    const { context, client} = this.props;
    const {orderItem, delivery} = context.state;
    if (delivery.from && delivery.till){
      const  price = await calculateTotalPrice({client, delivery, orderItem});
      if (price){
        this.setState({price});
      }
    }
  }

  onChangeNoItems(quantity){
    const { context } = this.props;
    const { orderItem } = context.state;
    context.setState({ orderItem: {...orderItem, quantity}}, () => this.loadEstimatedPrice());
  }

  render() {
    const {resources, tillInput} = this;
    const { context, busy, paymentCards, errors, navigationStrategy} = this.props;
    const { delivery, payment, orderItem, selectedContentType} = context.state;
    const { quantity: noRequiredForOffload } = orderItem;
    const { requireHelp, from_isDatePickerVisible, till_isDatePickerVisible, selectedCard, price } = this.state;

    const datePickerOptions = {
      datePickerModeAndroid: 'calendar',
      mode: 'datetime',
      titleIOS: 'Select delivery time',
      minimumDate: moment().startOf('day').toDate(),
      maximumDate: moment().add(14, 'days').toDate()
    };

    const validationSchema = {};

    if (selectedContentType.fromTime){
      validationSchema.from = yup.date().required();
    }

    if (selectedContentType.tillTime){
      validationSchema.till = yup.date().required();
    }

    const fixedPriceValidationSchema = {
      fixedPriceValue: yup.number().required()
    };
    

    return busy ?
      <LoadingScreen text="Loading Customer Cards" /> : <Container>
        <Header withButton>
          <Left>
            <Button onPress={() => navigationStrategy.prev()}>
              <Icon name='back-arrow'/>
            </Button>
          </Left>
          <Body><Title>{resources.PageTitle}</Title></Body>
        </Header>
        <Content>
          <ErrorRegion errors={errors}/>
          <ListItem padded>
            <Row>
              <OriginDestinationSummary contentType={selectedContentType} delivery={delivery}/>
            </Row>
          </ListItem>
          {resources.AllowFixedPrice ?
            <ListItem padded>
              <Row style={{width: '100%', flexDirection: 'row', justifyContent: 'flex-start'}}>
                <CheckBox onPress={() => this.toggleFixedPrice()} style={{marginRight: 10}}  categorySelectionCheckbox checked={delivery.isFixedPrice}/>
                {delivery.isFixedPrice ? <CurrencyInput
                  style={styles.input}
                  initialPrice={delivery.fixedPriceValue}
                  placeholder="Enter Fixed Price"
                  onValueChanged={this.onFixedPriceValueChanged}
                /> : <Text style={{fontSize: 18, fontWeight: 'bold'}}>{formatPrice(price / 100)}</Text>}
              </Row></ListItem> : null}
          { !delivery.isFixedPrice && selectedContentType.fromTime ?  <ListItem padded onPress={() => this.toggleDatePicker('from', true)}>
            <Icon paddedIcon name="delivery-time" />
            {delivery.from !== undefined ? <Text>{moment(delivery.from).format('dddd Do MMMM, h:mma')}</Text> : <Text grey>{resources.JobStartCaption}</Text>}
            <DatePicker cannedDateOptions={resources.CannedStartDateOptions} asapDateResolver={resources.AsapStartDateResolver}  isVisible={from_isDatePickerVisible} onCancel={() => this.toggleDatePicker('from', false)} onConfirm={(date) => {
              this.onChangeDate('from', date);
              if (tillInput){
                setTimeout(tillInput.attemptToSetCannedDate);
              }
            }} {...datePickerOptions} />
          </ListItem> : null}
          {!delivery.isFixedPrice && selectedContentType.tillTime ?  <ListItem padded onPress={() => this.toggleDatePicker('till', true)}>
            <Icon paddedIcon name="delivery-time" />
            {delivery.till !== undefined ? <Text>{moment(delivery.till).format('dddd Do MMMM, h:mma')}</Text> : <Text grey>{resources.JobEndCaption}</Text>}
            <DatePicker ref={ tillInput => { this.tillInput = tillInput;}}cannedDateOptions={resources.CannedEndDateOptions} from={delivery.from} asapDateResolver={resources.AsapEndDateResolver} isVisible={till_isDatePickerVisible} onCancel={() => this.toggleDatePicker('till', false)} onConfirm={(date) => this.onChangeDate('till', date)} {...datePickerOptions} />
          </ListItem> : null}
          {selectedCard ? <ListItem padded >
            <CardIcon brand={selectedCard.brand} /><Text>Pay with card</Text>
            <Picker style={styles.cardPicker} itemStyle={{height: 38}} selectedValue={payment.paymentId} onValueChange={(itemValue) => this.setCard(itemValue)}>
              {paymentCards.map(c => <Picker.Item key={c.id} label={`****${c.last4}  ${c.expMonth}/${c.expYear}`} value={c} />)}
            </Picker>
          </ListItem> : null}
          
          {selectedContentType.noPeople ?
            <ListItem padded style={{ borderBottomWidth: 0 }} onPress={() => this.setRequireHelp(!requireHelp)}>
              <CheckBox checked={requireHelp} categorySelectionCheckbox onPress={() => this.setRequireHelp(!requireHelp)} />
              <Text>{resources.NoPeopleCaption}</Text>
            </ListItem> : null}

          {selectedContentType.noPeople && requireHelp ? <ListItem paddedLeftRight style={{ borderBottomWidth: 0, borderTopWidth: 0 }}>
            <Grid>
              <Row>
                <Text style={{ paddingBottom: 5 }}>How many people do you need?</Text>
              </Row>
              <Row>
                <Col style={{ marginRight: 10 }}>
                  <Row>
                    <Button personButton active={noRequiredForOffload == 1} onPress={() => this.onChangeNoItems(1)} >
                      <Icon name='one-person' />
                    </Button>
                  </Row>
                  <Row style={styles.personSelectTextRow}>
                    <Text style={styles.personSelectText}>1</Text>
                  </Row>
                </Col>
                <Col style={{ marginRight: 10 }}>
                  <Row>
                    <Button personButton active={noRequiredForOffload == 2} onPress={() => this.onChangeNoItems(2)} >
                      <Icon name='two-people' />
                    </Button>
                  </Row>
                  <Row style={styles.personSelectTextRow}>
                    <Text style={styles.personSelectText}>2</Text>
                  </Row>
                </Col>
              </Row>
            </Grid>
          </ListItem> : null}
        </Content>
        <ValidatingButton fullWidth paddedBottom iconRight onPress={() => navigationStrategy.next()} validationSchema={delivery.isFixedPrice ? yup.object(fixedPriceValidationSchema) : yup.object(validationSchema)} validateOnMount={true} model={delivery}>
          <Text uppercase={false}>Continue</Text>
          <Icon next name='forward-arrow' />
        </ValidatingButton>
      </Container>;
  }
}


DeliveryOptions.PropTypes = {
  paymentCards: PropTypes.array,
  user: PropTypes.object
};

const styles = {
  cardPicker: {
    width: 200,
    height: 20
  },
  personSelectTextRow: {
    justifyContent: 'center'
  },
  personSelectText: {
    marginTop: 5,
    marginBottom: 25,
    fontSize: 16,
    textAlign: 'center'
  },
  input: {
    paddingHorizontal: 8,
    backgroundColor: '#FFFFFF',
    borderRadius: 4,
    padding: 10,
    width: 225,
    borderWidth: 0.5,
    borderColor: '#edeaea'
  },
};

const mapStateToProps = (state, initialProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  const {dispatch} = initialProps;
  const paymentCards = getDaoState(state, ['paymentCards'], 'paymentDao') || [];
  const defaultCard = paymentCards.find(c => c.id == user.stripeDefaultPaymentSource) || paymentCards[0];
  const {context} = initialProps;
  const {selectedContentType} = context.state;
  const getPaymentCardsIfNotAlreadyGot = () =>{
    dispatch(getPaymentCardsIfNotAlreadySucceeded());
  };
  return {
    getPaymentCardsIfNotAlreadyGot,
    ...initialProps,
    selectedContentType,
    ...getNavigationProps(initialProps),
    busy: isAnyOperationPending(state, [{ paymentDao: 'getCustomerPaymentCards' }]),
    errors: getOperationError(state, 'paymentDao', 'getCustomerPaymentCards' ),
   
    paymentCards,
    user,
    defaultCard
  };
};

export default withRouter(connect(
  mapStateToProps
)(DeliveryOptions));


