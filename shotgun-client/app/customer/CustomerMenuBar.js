import React, {Component, PropTypes} from 'react';
import { View, StyleSheet } from 'react-native';
import ActionButton from '../common/ActionButton';
import icon from  '../common/assets/cart-outline.png';
export default class CustomerMenuBar extends Component {
    static PropTypes = {
      shoppingCartDao: PropTypes.object,
      navigator: PropTypes.object
    };


    constructor(props){
      super(props);
      this.viewShoppingCart = this.viewShoppingCart.bind(this);
      this.updateItemCount = this.updateItemCount.bind(this);
      this.state = {};
    }

    componentWillMount(){
      this.subscription = this.props.shoppingCartDao.shoppingCartSizeObservable.subscribe(this.updateItemCount);
    }

    componentWillUnmount(){
      if (this.subscription){
        this.subscription.dispose();
      }
    }

    updateItemCount(itemCount){
      this.setState({itemCount});
    }

    viewShoppingCart(data){
      this.props.navigator.push({name: 'view-shopping-cart', data});
    }

    render(){
      const { itemCount } = this.state;
      const { shoppingCartDao } = this.props;
      const { rows } = shoppingCartDao;

      return <View style={styles.container}>
        <ActionButton buttonText={`(${itemCount})`} icon={icon} action={() => this.viewShoppingCart(rows)}/>
      </View>;
    }
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    marginTop: 60,
    flexDirection: 'row'
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  }
});
