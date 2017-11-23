import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, TextInput} from 'react-native';
import {Text, Button} from 'native-base';
import {isOperationPending, getOperationError} from 'common/dao';
import {connect} from 'react-redux';
import {addItemToCartAction} from 'customer/actions/CustomerActions';

class ProductActionBar extends Component {
    static PropTypes = {
      product: PropTypes.object,
      orderItemsDao: PropTypes.object
    };

    constructor(props){
      super(props);
      this.addToCart = this.addToCart.bind(this);
      this.state = {
        itemCount: 1,
        busy: false
      };
    }

    addToCart() {
      const {dispatch, product} = this.props;
      const {itemCount} = this.state;
      const {productId} = product;
      dispatch(addItemToCartAction({productId, quantity: itemCount}));
    }

    onItemCountChange(itemCount) {
      this.setState({itemCount});
    }

    render() {
      const {busy, itemCount} = this.state;
      return (
        <View style={styles.container}>
          <TextInput
            keyboardType = 'numeric'
            onChangeText = {(text)=> this.onItemCountChange(text)}
            value = {itemCount + ''}
            maxLength = {10}  //setting limit of input
          />
          {!busy ? <Button onPress={this.addToCart}><Text>Add To Basket</Text></Button> : null}
        </View>
      );
    }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isOperationPending(state, 'cartItemsDao', 'addItemToCart'),
  errors: getOperationError(state, 'cartItemsDao', 'addItemToCart'),
  ...nextOwnProps
});

const ConnectedProductActionBar =  connect(mapStateToProps)(ProductActionBar);

export default ConnectedProductActionBar;

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    backgroundColor: '#FAFAFF',
    paddingVertical: 8
  },
  action: {
    flex: 1,
    alignItems: 'center'
  },
  actionText: {
    color: '#007AFF'
  },
  icon: {
    height: 20,
    width: 20
  }
});
