import React, {Component} from 'react';
import PropTypes from 'prop-types';
import * as constants from 'common/dao/ActionConstants';
import {connect} from 'react-redux';
import {View, Text, Picker} from 'react-native';
import ActionButton from '../../common/components/ActionButton';

class Payment extends Component {
  constructor(props) {
    super(props);
    this.setCard = this.setCard.bind(this);
  }

  componentWillMount(){
    this.setCard(this.props.customer.paymentCards.find(c => c.isDefault).paymentId);
  }

  setCard(paymentId){
    this.props.dispatch({type: constants.UPDATE_ORDER, order: {paymentId}});
  }

  render() {
    const {customer, navigation, order} = this.props;

    return <View style={{flex: 1, flexDirection: 'column'}}>
      <Text>Payment Details</Text>
      <Picker selectedValue={order.paymentId} onValueChange={(itemValue) => this.setCard(itemValue)}>
        {customer.paymentCards.map(c => <Picker.Item  key={c.paymentId} label={`${c.cardNumber}  ${c.expiryDate}`} value={c.paymentId} />)}
      </Picker>
      <ActionButton buttonText="Next" icon={null} action={() => navigation.navigate('Delivery')}/>
    </View>;
  }
}

Payment.PropTypes = {
  status: PropTypes.object,
  customer: PropTypes.object,
  order: PropTypes.object
};

Payment.navigationOptions = {header: null};

const mapStateToProps = ({CheckoutReducer, CustomerReducer}) => ({
  customer: CustomerReducer.customer,
  status: CheckoutReducer.status,
  order: CheckoutReducer.order
});

export default connect(
  mapStateToProps
)(Payment);
