import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'custom-redux';
import { Picker } from 'react-native';
import {Button, Container, ListItem, Header, Text, Title, Body, Left, Grid, Row, Col, Content, CheckBox } from 'native-base';
import { merge } from 'lodash';
import { withRouter } from 'react-router';
import {getDaoState, isAnyOperationPending, getOperationError, getNavigationProps} from 'common/dao';
import {LoadingScreen, ValidatingButton, CardIcon, ErrorRegion, Icon, OriginDestinationSummary} from 'common/components';
import DatePicker from 'common/components/datePicker/DatePicker';
import moment from 'moment';
import yup from 'yup';

class DeliveryOptions extends Component {
  constructor(props) {
    super(props);
    this.onChangeValue = this.onChangeValue.bind(this);
    this.setRequireHelp = this.setRequireHelp.bind(this);
    this.setCard = this.setCard.bind(this);
    this.state = {
      requireHelp: false,
      from_isDatePickerVisible: false,
      till_isDatePickerVisible: false,
      selectedCard: undefined,
      date: undefined
    };
  }

  componentDidMount() {
    if (this.props.defaultCard !== undefined) {
      this.setCard(this.props.defaultCard);
    }
  }

  componentWillReceiveProps(nextProps) {
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

  onChangeValue(field, value) {
    const { context } = this.props;
    const { delivery } = context.state;
    context.setState({ delivery: merge({}, delivery, { [field]: value }) });
  }

  onChangeDate(field, value) {
    this.onChangeValue(field, value);
    this.toggleDatePicker(field, false);
  }

  onChangeNoItems(quantity){
    const { context } = this.props;
    const { orderItem } = context.state;
    context.setState({ orderItem: merge({}, orderItem, {quantity}) });
  }

  render() {
    const { context, busy, paymentCards, errors, navigationStrategy} = this.props;
    const { delivery, payment, orderItem, selectedContentType} = context.state;
    const { quantity: noRequiredForOffload } = orderItem;
    const { requireHelp, from_isDatePickerVisible, till_isDatePickerVisible, selectedCard } = this.state;

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
    

    return busy  || !selectedCard ?
      <LoadingScreen text="Loading Customer Cards" /> : <Container>
        <Header withButton>
          <Left>
            <Button onPress={() => navigationStrategy.prev()}>
              <Icon name='back-arrow'/>
            </Button>
          </Left>
          <Body><Title>Delivery Details</Title></Body>
        </Header>
        <Content>
          <ErrorRegion errors={errors}/>
          <ListItem padded>
            <OriginDestinationSummary contentType={selectedContentType} delivery={delivery}/>
          </ListItem>
          {selectedContentType.fromTime ?  <ListItem padded onPress={() => this.toggleDatePicker('from', true)}>
            <Icon paddedIcon name="delivery-time" />
            {delivery.from !== undefined ? <Text>{moment(delivery.from).format('dddd Do MMMM, h:mma')}</Text> : <Text grey>Set a collection time</Text>}
            <DatePicker isVisible={from_isDatePickerVisible} onCancel={() => this.toggleDatePicker('from', false)} onConfirm={(date) => this.onChangeDate('from', date)} {...datePickerOptions} />
          </ListItem> : null}
          {selectedContentType.tillTime ?  <ListItem padded onPress={() => this.toggleDatePicker('till', true)}>
            <Icon paddedIcon name="delivery-time" />
            {delivery.till !== undefined ? <Text>{moment(delivery.till).format('dddd Do MMMM, h:mma')}</Text> : <Text grey>Set a return time</Text>}
            <DatePicker isVisible={till_isDatePickerVisible} onCancel={() => this.toggleDatePicker('till', false)} onConfirm={(date) => this.onChangeDate('till', date)} {...datePickerOptions} />
          </ListItem> : null}
          <ListItem padded >
            <CardIcon brand={selectedCard.brand} /><Text>Pay with card</Text>
            <Picker style={styles.cardPicker} selectedValue={payment.paymentId} onValueChange={(itemValue) => this.setCard(itemValue)}>
              {paymentCards.map(c => <Picker.Item key={c.id} label={`****${c.last4}  ${c.expMonth}/${c.expYear}`} value={c} />)}
            </Picker>
          </ListItem>
          
          {selectedContentType.noPeople ?
            <ListItem padded style={{ borderBottomWidth: 0 }} onPress={() => this.setRequireHelp(!requireHelp)}>
              <CheckBox checked={requireHelp} onPress={() => this.setRequireHelp(!requireHelp)} />
              <Text style={{ paddingLeft: 10 }}>Do you need more than one person ?(s)</Text>
            </ListItem> : null}

          {selectedContentType.noPeople && requireHelp ? <ListItem paddedLeftRight style={{ borderBottomWidth: 0, borderTopWidth: 0 }}>
            <Grid>
              <Row>
                <Text style={{ paddingBottom: 15 }}>How many people do you need?</Text>
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
        <ValidatingButton fullWidth paddedBottom iconRight onPress={() => navigationStrategy.next()} validationSchema={yup.object(validationSchema)} validateOnMount={true} model={delivery}>
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
  }
};

const mapStateToProps = (state, initialProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  const paymentCards = getDaoState(state, ['paymentCards'], 'paymentDao') || [];
  const defaultCard = paymentCards.find(c => c.id == user.stripeDefaultPaymentSource) || paymentCards[0];

  return {
    ...initialProps,
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


