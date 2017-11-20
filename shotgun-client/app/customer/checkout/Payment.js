import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {View, Text, Picker} from 'react-native';
import ActionButton from '../../common/components/ActionButton';
import {getDaoState} from 'common/dao';
const DEFAULT_PAYMENT_CARDS = [];
class Payment extends Component {
  static propTypes = {
    paymentCards: PropTypes.array
  };

  static navigationOptions = {header: null};

  constructor(props) {
    super(props);
    this.setCard = this.setCard.bind(this);
    this.state = {};
  }

  componentWillMount(){
    const {paymentCards} = this.props;
    const defaultCard = paymentCards.find(c => c.isDefault) || paymentCards[0];
    if (defaultCard){
      this.setCard(defaultCard.paymentId);
    }
  }

  setCard(paymentId){
    this.setState({paymentId});
  }

  render() {
    const {paymentCards, navigation} = this.props;
    const {paymentId} = this.state;

    return <View style={{flex: 1, flexDirection: 'column'}}>
      <Text>Payment Details</Text>
      <Picker selectedValue={paymentId} onValueChange={(itemValue) => this.setCard(itemValue)}>
        {paymentCards.map(c => <Picker.Item  key={c.paymentId} label={`${c.cardNumber}  ${c.expiryDate}`} value={c.paymentId} />)}
      </Picker>
      {paymentId ? <ActionButton buttonText="Next" icon={null} action={() => navigation.navigate('Delivery', {paymentId})}/> : null}
    </View>;
  }
}

const mapStateToProps = (state, initialProps) => ({
  paymentCards: getDaoState(state, ['customer', 'paymentCards'], 'paymentCardsDao') || DEFAULT_PAYMENT_CARDS,
  ...initialProps
});

export default connect(
  mapStateToProps
)(Payment);
