import React, {Component, PropTypes} from 'react';
import {View, Text, Picker} from 'react-native';
import ActionButton from '../../common/components/ActionButton';

export default class Payment extends Component {
  static PropTypes = {
    customerService: PropTypes.object
  };

  static navigationOptions = {header: null};

  constructor(props) {
    super(props);
    this.updateCardItems = this.updateCardItems.bind(this);
    this.setCard = this.setCard.bind(this);
    this.customerService = this.props.screenProps.customerService;
    this.navigation = this.props.navigation;
    this.state = {
      paymentCards: [],
      order: this.props.navigation.state.params.order,
      delivery: this.props.navigation.state.params.delivery
    };
  }

  componentWillMount(){
    this.paymentCardSubscription = this.customerService.paymentCardsDao.onSnapshotCompleteObservable.subscribe(this.updateCardItems);
  }

  componentWillUnmount(){
    if (this.paymentCardSubscription){
      this.paymentCardSubscription.dispose();
    }
  }

  updateCardItems(paymentCards){
    this.setState({paymentCards});
    this.setCard(paymentCards.find(c => c.isDefault).paymentId);
  }

  setCard(paymentId){
    this.setState({order: Object.assign({}, this.state.order, {paymentId})});
  }

  render() {
    const {paymentCards} = this.state;

    return <View style={{flex: 1, flexDirection: 'column'}}>
      <Text>Payment Details</Text>
      <Picker selectedValue={this.state.order.paymentId} onValueChange={(itemValue) => this.setCard(itemValue)}>
        {paymentCards.map(c => <Picker.Item  key={c.paymentId} label={`${c.cardNumber}  ${c.expiryDate}`} value={c.paymentId} />)}
      </Picker>

      <ActionButton buttonText="Next" icon={null} action={() => this.navigation.navigate('Delivery', {order: this.state.order, delivery: this.state.delivery})}/>
    </View>;
  }
}
