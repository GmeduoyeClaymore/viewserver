import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {View, Text, Picker} from 'react-native';
import ActionButton from '../../common/components/ActionButton';
import {getDaoState} from 'common/dao';

class Payment extends Component {
  static propTypes = {
    customer: PropTypes.object
  };

  static navigationOptions = {header: null};

  constructor(props) {
    super(props);
    this.setCard = this.setCard.bind(this);
    this.state = {};
  }

  componentWillMount(){
    this.setCard(this.props.customer.paymentCards.find(c => c.isDefault).paymentId);
  }

  setCard(paymentId){
    this.setState({paymentId});
  }

  render() {
    const {customer, navigation} = this.props;
    const {paymentId} = this.state;

    return <View style={{flex: 1, flexDirection: 'column'}}>
      <Text>Payment Details</Text>
      <Picker selectedValue={paymentId} onValueChange={(itemValue) => this.setCard(itemValue)}>
        {customer.paymentCards.map(c => <Picker.Item  key={c.paymentId} label={`${c.cardNumber}  ${c.expiryDate}`} value={c.paymentId} />)}
      </Picker>
      <ActionButton buttonText="Next" icon={null} action={() => navigation.navigate('Delivery', {paymentId})}/>
    </View>;
  }
}

const mapStateToProps = (state, initialProps) => ({
  customer: getDaoState(state, [], 'customerDao'),
  ...initialProps
});

export default connect(
  mapStateToProps
)(Payment);
