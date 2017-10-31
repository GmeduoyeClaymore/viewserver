import React, {Component, PropTypes} from 'react';
import { View, StyleSheet } from 'react-native';
import ActionButton from '../common/components/ActionButton';
import icon from  '../common/assets/cart-outline.png';
export default class CustomerMenuBar extends Component {
    static PropTypes = {
      cartSummaryDao: PropTypes.object,
      navigation: PropTypes.object
    };


    constructor(props){
      super(props);
      this.updateTotalQuantity = this.updateTotalQuantity.bind(this);
      this.state = {totalQuantity: 0};
    }

    componentWillMount(){
      this.subscription = this.props.cartSummaryDao.subscribe(this.updateTotalQuantity);
    }

    componentWillUnmount(){
      if (this.subscription){
        this.subscription.dispose();
      }
    }

    updateTotalQuantity(cartSummary){
      const totalQuantity = cartSummary ? cartSummary.totalQuantity : 0;
      this.setState({totalQuantity});
    }

    render(){
      const { totalQuantity } = this.state;
      const {navigate} = this.props.navigation;
      return <View style={styles.container}>
        <ActionButton buttonText={`(${totalQuantity})`} icon={icon} action={() => navigate('Cart')}/>
      </View>;
    }
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    paddingTop: 10,
    flexDirection: 'row'
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  }
});
