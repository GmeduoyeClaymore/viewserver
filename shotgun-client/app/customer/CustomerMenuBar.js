import React, {Component, PropTypes} from 'react';
import { View, StyleSheet } from 'react-native';
import ActionButton from '../common/components/ActionButton';
import cartIcon from  '../common/assets/cart-outline.png';
import homeIcon from  '../common/assets/home.png';
import orderIcon from  '../common/assets/orders.png';
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
rr
    updateTotalQuantity(cartSummary){
      const totalQuantity = cartSummary ? cartSummary.totalQuantity : 0;
      this.setState({totalQuantity});
    }

    render(){
      const { totalQuantity } = this.state;
      const {navigate} = this.props.navigation;
      return <View style={styles.container}>
        <ActionButton buttonText={null} icon={homeIcon} action={() => navigate('ProductCategoryList')}/>
        <ActionButton buttonText={`(${totalQuantity})`} icon={cartIcon} action={() => navigate('Cart')}/>
        <ActionButton buttonText={null} icon={orderIcon} action={() => navigate('Orders')}/>
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
