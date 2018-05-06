import React, { Component } from 'react';
import DatePicker from 'common/components/datePicker/DatePicker';
import {withExternalState} from 'custom-redux';
import {Text, Row, Col,  Item, Label, Spinner} from 'native-base';
import {ValidatingInput, ValidatingButton, CurrencyInput, Icon} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import moment from 'moment';
const WaitingResponse = () => (<Row  style={{padding: 5}}><Spinner style={{height: 15}}/><Text>{'Waiting for Custumer response..'}</Text></Row>);
const ResponseAccepted = ({customer}) => (<Row  style={{padding: 5}}><Icon name='star' style={styles.star}/><Text style={{padding: 5}}>{`${customer.firstName} ${customer.lastName} Accepted`}</Text></Row>);

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
      const {negotiationAmount, negotiationDate, onOrderRespond} = this.props;
      if (onOrderRespond){
        onOrderRespond({negotiationAmount, negotiationDate});
      }
    }

    render() {
      const {busyUpdating, order} = this.props;
      let {negotiationAmount, negotiationDate} = this.props;
      const {negotiatedResponseStatus, customer} = order;
      negotiationAmount = negotiationAmount || order.amount;
      negotiationDate = negotiationDate || order.requiredDate;
      const {isDatePickerVisible} = this.state;
      const hasResponded = negotiatedResponseStatus === 'RESPONDED';
      const isAccepted = negotiatedResponseStatus === 'ASSIGNED';

      const StatusControl = () => {
        if (hasResponded){
          return  <WaitingResponse/>;
        }
        if (isAccepted){
          return  <ResponseAccepted customer={customer}/>;
        }
        return <ValidatingButton busy={busyUpdating} fullWidth success onPress={this.respondToOrder} validateOnMount={true} validationSchema={yup.object(validationSchema)} model={{negotiationAmount, negotiationDate}}>
          <Text uppercase={false}>Respond To Job</Text>
        </ValidatingButton>;
      };

      return (
        <Col>
          <Row>
            <Item stackedLabel  style={{marginLeft: 4, marginRight: 10, flex: 1, marginBottom: 10}} >
              <Label>Your Price</Label>
              <CurrencyInput disabled={hasResponded} value={negotiationAmount} style={{width: '100%'}} ref={ip => {this.amountInput = ip;}} onValueChange={this.setNegotiationAmount} placeholder="Enter your price"/>
            </Item>
            <Item stackedLabel  style={{marginLeft: 4, marginRight: 10, flex: 1, marginBottom: 10}} >
              <Label>Avialiability Date</Label>
              <ValidatingInput  disabled={hasResponded} onPress={() => this.toggleDatePicker(true)} editable={false} bold
                value={negotiationDate ? moment(negotiationDate).format('DD MMM YY') : undefined}
                placeholder="Enter Availability Date" validateOnMount={negotiationDate !== undefined}
                validationSchema={validationSchema.negotiationAmount} maxLength={10}/>
              <DatePicker disabled={hasResponded} isVisible={isDatePickerVisible} cannedDateOptions={[]}
                onCancel={() => this.toggleDatePicker(false)}
                onConfirm={(value) => this.onChangeDate(value)} {...datePickerOptions} />
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
  }
};

const validationSchema = {
  negotiationAmount: yup.number().required(),
  negotiationDate: yup.date().required()
};
  
const datePickerOptions = {
  datePickerModeAndroid: 'calendar',
  mode: 'datetime',
  titleIOS: 'Select delivery time',
  maximumDate: moment().add(14, 'days').toDate()
};


export default withExternalState()(PartnerNegotiationPanel);
