import React, { Component } from 'react';
import DatePicker from 'common/components/datePicker/DatePicker';
import {withExternalState} from 'custom-redux';
import {Text, Row, Col,  Item, Label, Spinner} from 'native-base';
import {ValidatingInput, ValidatingButton, CurrencyInput, Icon, SpinnerButton} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import moment from 'moment';
import { cancelResponsePartner } from 'partner/actions/PartnerActions';

const CancelResponse = ({ orderId, orderContentTypeId, busyUpdating, dispatch, ...rest }) => {
  const onCancelResponse = () => {
    dispatch(cancelResponsePartner(orderId, orderContentTypeId));
  };
  return <SpinnerButton {...rest} padded busy={busyUpdating} style={{ alignSelf: 'flex-start', flex: 1, marginRight: 0, marginLeft: 10, maxHeight: 25 }} danger fullWidth onPress={onCancelResponse}><Text style={{fontSize: 8}} uppercase={false}>Cancel</Text></SpinnerButton>;
};


const WaitingResponse = (props) => (<Row  style={{padding: 5, marginTop: 15,  height: 25 }}><Spinner style={{height: 15, marginRight: 10}}/><Text>{'Waiting for custumer response..'}</Text><CancelResponse {...props}/></Row>);
const Response = ({customer, hasAccepted}) => (<Row  style={{padding: 5, marginBottom: 5}}><Icon name='star' style={hasAccepted ? styles.starAccepted : styles.starDeclined  }/><Text style={{paddingTop: 15, paddingLeft: 5}}>{`${customer.firstName} ${customer.lastName} ${hasAccepted ? 'Accepted' : 'Declined'}`}</Text></Row>);

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

    respondToOrder = () => {
      let  {negotiationAmount, negotiationDate} = this.props;
      const {onOrderRespond, order} = this.props;
      const {responseInfo} = order;
      const { responsePrice, responseDate} = responseInfo;
      negotiationAmount = negotiationAmount || responsePrice || (order.amount);
      negotiationDate = negotiationDate || responseDate || order.requiredDate;
      if (onOrderRespond){
        onOrderRespond({negotiationAmount, negotiationDate});
      }
    }

    render() {
      const {busyUpdating, order} = this.props;
      let {negotiationAmount, negotiationDate} = this.props;
      const {responseInfo, customer} = order;
      const {responseStatus, responsePrice, responseDate} = responseInfo;
      negotiationAmount = negotiationAmount || responsePrice || (order.amount);
      negotiationDate = negotiationDate || responseDate || order.requiredDate;
      const {isDatePickerVisible} = this.state;
      const awaitingCustomerResponse = responseStatus === 'RESPONDED';
      const hasCustomerResponded = !!~['DECLINED', 'ACCEPTED'].indexOf(responseStatus);
      const hasAccepted = responseStatus === 'ACCEPTED';

      const StatusControl = () => {
        if (awaitingCustomerResponse){
          return  <WaitingResponse orderId={order.orderId} orderContentTypeId={order.orderContentTypeId} {...this.props} />;
        }
        return [hasCustomerResponded ? <Response key="1" customer={customer} hasAccepted={hasAccepted}/> : null, <ValidatingButton  key="2" busy={busyUpdating} fullWidth success onPress={this.respondToOrder} validateOnMount={!awaitingCustomerResponse} validationSchema={yup.object(validationSchema)} model={{negotiationAmount, negotiationDate}}>
          <Text uppercase={false}>Respond To Job</Text>
        </ValidatingButton>];
      };

      return (
        <Col style={{padding: 20}}>
          <Row>
            <Item stackedLabel  style={{marginLeft: 4, marginRight: 10, flex: 1, marginBottom: 10}} >
              <Label>Your Price</Label>
              <CurrencyInput disabled={awaitingCustomerResponse} initialPrice={negotiationAmount} style={{width: '100%', paddingTop: 10, paddingBottom: 10}} ref={ip => {this.amountInput = ip;}} onValueChanged={this.setNegotiationAmount} placeholder="Enter your price"/>
            </Item>
            <Item stackedLabel  style={{marginLeft: 4, marginRight: 10, flex: 1, marginBottom: 10}} >
              <Label>Avialiability Date</Label>
              <ValidatingInput  disabled={awaitingCustomerResponse} onPress={() => this.toggleDatePicker(true)} editable={!awaitingCustomerResponse} bold
                value={negotiationDate ? moment(negotiationDate).format('DD MMM YY') : undefined}
                placeholder="Enter Availability Date" validateOnMount={negotiationDate !== undefined}
                validationSchema={validationSchema.negotiationAmount} maxLength={10}/>
              {awaitingCustomerResponse ? null : <DatePicker disabled={awaitingCustomerResponse} isVisible={isDatePickerVisible} cannedDateOptions={[]}
                onCancel={() => this.toggleDatePicker(false)}
                onConfirm={(value) => this.onChangeDate(value)} {...datePickerOptions} />}
            </Item>
           
          </Row>
          <StatusControl/>
        </Col>
      );
    }
}

const styles = {
  star: {
    paddingTop: 10,
    alignSelf: 'center',
    fontSize: 30,
    padding: 2,
    color: shotgun.brandSuccess,
  },
  starAccepted: {
    paddingTop: 10,
    alignSelf: 'center',
    fontSize: 30,
    padding: 2,
    color: shotgun.brandSuccess,
  },
  starDeclined: {
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
