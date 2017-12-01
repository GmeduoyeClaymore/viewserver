import React, {Component} from 'react';
import PropTypes from 'prop-types';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {Text} from 'native-base';
import {View} from 'react-native';
import {isOperationPending, getOperationError} from 'common/dao';
import {connect} from 'react-redux';
import {addItemToCartAction} from 'customer/actions/CustomerActions';
import yup from 'yup';

class ProductActionBar extends Component {
    static PropTypes = {
      product: PropTypes.object,
      orderItemsDao: PropTypes.object
    };

    static validationSchema = {
      quantity: yup.string()
        .matches(/^([1-9][0-9]{0,2})$/)
        .required()
    };

    constructor(props){
      super(props);
      this.addToCart = this.addToCart.bind(this);
      this.onQuantityChange = this.onQuantityChange.bind(this);
      this.state = {
        quantity: 1,
        busy: false
      };
    }

    addToCart() {
      const {dispatch, product} = this.props;
      const {quantity} = this.state;
      const {productId} = product;
      dispatch(addItemToCartAction({productId, quantity}));
    }

    onQuantityChange(quantity) {
      this.setState({quantity});
    }

    render() {
      const {busy, quantity} = this.state;
      return (
        <View>
          <ValidatingInput keyboardType = 'numeric' onChangeText={(value)=> this.onQuantityChange(value)} validationSchema={ProductActionBar.validationSchema.quantity} showIcons={false} validateOnMount={true} value={`${quantity}`}/>
          <ValidatingButton onPress={this.addToCart} disabled={busy} validateOnMount={true} validationSchema={ProductActionBar.validationSchema.quantity} model={`${quantity}`}><Text>Add To Basket</Text></ValidatingButton>
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
