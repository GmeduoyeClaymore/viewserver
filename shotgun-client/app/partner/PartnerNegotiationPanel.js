import React, { Component } from 'react';
import DatePicker from 'common/components/datePicker/DatePicker';
import {withExternalState} from 'custom-redux';
import {Text, Row, View, Spinner, Item, Label, Col} from 'native-base';
import {ValidatingInput, ValidatingButton, CurrencyInput, Icon, SpinnerButton} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import moment from 'moment';
import { cancelResponsePartner } from 'partner/actions/PartnerActions';
import { Platform } from 'react-native';
const IS_ANDROID = Platform.OS === 'android';

class PartnerNegotiationPanel extends Component {
  state = {};

  toggleDatePicker = (isDatePickerVisible) => {
    super.setState({isDatePickerVisible});
  }

  onChangeDate = (negotiationDate) => {
    this.setState({negotiationDate});
    this.toggleDatePicker(false);
  }

  setNegotiationAmount = (negotiationAmount) => {
    this.setState({negotiationAmount});
  }

  respondToOrder = (onOrderRespond, negotiationAmount, negotiationDate) => {
    if (onOrderRespond){
      onOrderRespond({negotiationAmount, negotiationDate});
    }
  }

  onCancelResponse = () => {
    const {dispatch, order} = this.props;
    const {orderId, orderContentTypeId} = order;
    dispatch(cancelResponsePartner(orderId, orderContentTypeId));
  };

  render() {
    const {busyUpdating, order, onOrderRespond} = this.props;
    let {negotiationAmount, negotiationDate} = this.props;
    const {isDatePickerVisible} = this.state;
    const {responseInfo, customer} = order;
    const {responseStatus} = responseInfo;
    const {responsePrice, responseDate} = responseInfo;

    negotiationAmount = negotiationAmount || responsePrice || (order.amount);
    negotiationDate = negotiationDate || responseDate || order.requiredDate;
    const awaitingCustomerResponse = responseStatus === 'RESPONDED';
    const hasCustomerResponded = !!~['DECLINED', 'ACCEPTED'].indexOf(responseStatus);
    const hasAccepted = responseStatus === 'ACCEPTED';

    return (
      <View padded>
        <Row>
          <Item stackedLabel style={styles.responseItem} >
            <Label>Your Price</Label>
            <CurrencyInput disabled={awaitingCustomerResponse} initialPrice={negotiationAmount} style={styles.currencyInput} ref={ip => {this.amountInput = ip;}} onValueChanged={this.setNegotiationAmount} placeholder="Enter your price"/>
          </Item>
          <Item stackedLabel style={styles.responseItem} >
            <Label>Availability Date</Label>
            <ValidatingInput disabled={awaitingCustomerResponse} onPress={() => this.toggleDatePicker(true)} editable={!awaitingCustomerResponse} bold
              value={negotiationDate ? moment(negotiationDate).format('DD MMM YY') : undefined}
              placeholder="Enter Availability Date" validateOnMount={negotiationDate !== undefined}
              validationSchema={validationSchema.negotiationAmount} maxLength={10} showIcons={false}/>
            {awaitingCustomerResponse ? null : <DatePicker disabled={awaitingCustomerResponse} isVisible={isDatePickerVisible} cannedDateOptions={[]}
              onCancel={() => this.toggleDatePicker(false)}
              onConfirm={(value) => this.onChangeDate(value)} {...datePickerOptions} />}
          </Item>
        </Row>

        {awaitingCustomerResponse ?
          <Row>
            <Col width={60}>
              <Row>
                <Spinner size={IS_ANDROID ? 30 : 1} color={shotgun.brandWarning} style={styles.waitingSpinner}/>
                <Text style={{alignSelf: 'center'}} numberOfLines={1}>Awaiting customer response</Text>
              </Row>
            </Col>
            <Col width={40}>
              <SpinnerButton style={styles.cancelButton} danger fullWidth onPress={this.onCancelResponse}>
                <Text uppercase={false}>Withdraw</Text>
              </SpinnerButton>
            </Col>
          </Row> : null}

        {hasCustomerResponded && !awaitingCustomerResponse ?
          <Row style={{padding: 5, marginBottom: 5}}>
            <Icon name={hasAccepted ? 'checkmark' : 'cross'} style={[styles.responseIcon, {color: hasAccepted ? shotgun.brandSuccess : shotgun.brandDanger}]}/>
            <Text style={{paddingTop: 15, paddingLeft: 5}}>{`${customer.firstName} ${customer.lastName} ${hasAccepted ? 'accepted' : 'declined'} your offer`}</Text>
          </Row> : null}

        {!awaitingCustomerResponse ?
          <ValidatingButton busy={busyUpdating} fullWidth success onPress={() => this.respondToOrder(onOrderRespond, negotiationAmount, negotiationDate)} validateOnMount={!awaitingCustomerResponse} validationSchema={yup.object(validationSchema)} model={{negotiationAmount, negotiationDate}}>
            <Text uppercase={false}>Respond To Job</Text>
          </ValidatingButton> : null}
      </View>
    );
  }
}

const styles = {
  responseItem: {
    marginLeft: 4,
    marginRight: 10,
    flex: 1,
    marginBottom: 10
  },
  currencyInput: {
    width: '100%',
    paddingTop: 10,
    paddingBottom: 10
  },
  cancelButton: {
    alignSelf: 'flex-end',
    flex: 1,
    marginRight: 0,
    marginLeft: 10
  },
  waitingSpinner: {
    height: 15,
    marginRight: 10,
    alignSelf: 'center'
  },
  responseIcon: {
    paddingTop: 10,
    alignSelf: 'center',
    fontSize: 30,
    padding: 2,
    color: shotgun.brandDanger,
  }
};

const validationSchema = {
  negotiationAmount: yup.number().required(),
  negotiationDate: yup.date().required()
};

const datePickerOptions = {
  datePickerModeAndroid: 'calendar',
  mode: 'date',
  titleIOS: 'Select start date',
  maximumDate: moment().add(14, 'days').toDate()
};

export default withExternalState()(PartnerNegotiationPanel);
