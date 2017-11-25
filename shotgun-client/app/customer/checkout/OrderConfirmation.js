import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {View} from 'react-native';
import {Spinner,  Container, Content, Header, Text, Title, Body, Left, Button, Icon} from 'native-base';
import {purchaseCartItemsAction} from 'customer/actions/CustomerActions';
import {getDaoState, isAnyOperationPending, getOperationError} from 'common/dao';
import ErrorRegion from 'common/components/ErrorRegion';

const OrderConfirmation = ({dispatch, history, errors, busy, payment, delivery, paymentCard, deliveryAddress, cart, summary}) => {
  const purchase = async() => {
    dispatch(purchaseCartItemsAction(delivery.eta, payment.paymentId, delivery.deliveryAddressId, delivery.deliveryType,  () => history.push('/CustomerLanding/Checkout/OrderComplete')));
  };

  const renderCartItem = (item) => {
    return  <View key={item.key} style={{flexDirection: 'column', flex: 1}}>
      <Text>{`Product: ${item.name} - (${item.productId})`}</Text>
      <Text>{`Quantity: ${item.quantity}`}</Text>
      <Text>{`Price: ${item.price}`}</Text>
      <Text>{`Total: ${item.totalPrice}`}</Text>
    </View>;
  };

  return <Container>
    <Header>
      <Left>
        <Button transparent>
          <Icon name='arrow-back' onPress={() => history.goBack()} />
        </Button>
      </Left>
      <Body><Title>Confirm Order</Title></Body>
    </Header>
    <Content>
      <ErrorRegion errors={errors}><View style={{flex: 1, flexDirection: 'column'}}>
        {cart.items.map(c => renderCartItem(c))}
        <Text>{`Total Items ${summary.totalQuantity}`}</Text>
        <Text>{`Total Price ${summary.totalPrice}`}</Text>
        <Text>Payment {paymentCard.cardNumber}</Text>
        <Text>Delivery Details {deliveryAddress.line1}</Text>
        <Text>Delivery Requested in {delivery.eta} hours</Text>
        {!busy ? <Button onPress={purchase}><Text>Place Order</Text></Button> :  <Spinner />}
      </View></ErrorRegion>
    </Content>
  </Container>;
};

OrderConfirmation.PropTypes = {
  status: PropTypes.object,
  cart: PropTypes.object,
  summary: PropTypes.object,
  order: PropTypes.object,
  delivery: PropTypes.object,
  customer: PropTypes.object
};

const mapStateToProps = (state, initialProps) => {
  const {context} = initialProps;
  const {delivery, payment} = context.state;

  const deliveryAddresses = getDaoState(state, ['customer', 'deliveryAddresses'], 'deliveryAddressDao');
  const paymentCards =  getDaoState(state, ['customer', 'paymentCards'], 'paymentCardsDao');
  const deliveryAddress = deliveryAddresses ? deliveryAddresses.find(a => a.deliveryAddressId == delivery.deliveryAddressId) : {};
  const paymentCard = paymentCards ? paymentCards.find(a => a.paymentId == payment.paymentId) : {};
  
  return {
    cart: getDaoState(state, ['cart'], 'cartItemsDao'),
    summary: getDaoState(state, [], 'cartSummaryDao'),
    errors: getOperationError(state, 'cartItemsDao', 'purchaseCartItems'),
    deliveryAddress,
    paymentCard,
    delivery,
    payment,
    busy: isAnyOperationPending(state, { cartItemsDao: 'purchaseCartItems'}),
    ...initialProps
  };
};
export default connect(
  mapStateToProps
)(OrderConfirmation);

