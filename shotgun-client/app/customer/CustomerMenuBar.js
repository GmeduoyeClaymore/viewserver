import React, {Component, PropTypes} from 'react';
import { View, StyleSheet } from 'react-native';
import ActionButton from '../common/ActionButton';
import icon from  '../common/assets/cart-outline.png';
export default class CustomerMenuBar extends Component {
    static PropTypes = {
      orderItemsDao: PropTypes.object,
      navigation: PropTypes.object
    };


    constructor(props){
      super(props);
      this.updateItemCount = this.updateItemCount.bind(this);
      this.state = {};
    }

    componentWillMount(){
      this.subscription = this.props.orderItemsDao.shoppingCartSizeObservable.subscribe(this.updateItemCount);
    }

    componentWillUnmount(){
      if (this.subscription){
        this.subscription.dispose();
      }
    }

    updateItemCount(itemCount){
      this.setState({itemCount});
    }

    render(){
      const { itemCount } = this.state;
      const {navigate} = this.props.navigation;
      return <View style={styles.container}>
        <ActionButton buttonText={`(${itemCount})`} icon={icon} action={() => navigate('Cart')}/>
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
