import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {View} from 'react-native';
import {Spinner,  Container, Content, Header, Text, Title, Body, Left, Button, Icon} from 'native-base';
import {checkout} from 'customer/actions/CustomerActions';
import {isAnyOperationPending, getOperationError, isAnyLoading} from 'common/dao';
import ErrorRegion from 'common/components/ErrorRegion';

const OrderConfirmation = ({dispatch, history, errors, busy, orderItem, payment, delivery}) => {
  const purchase = async() => {
    dispatch(checkout(orderItem, payment, delivery, () => history.push('/Customer/Checkout/OrderComplete')));
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
        <Text>Delivery Requested in {delivery.eta} hours</Text>
        {!busy ? <Button onPress={purchase}><Text>Place Order</Text></Button> :  <Spinner />}
      </View></ErrorRegion>
    </Content>
  </Container>;
};

OrderConfirmation.PropTypes = {
  orderItem: PropTypes.object,
  delivery: PropTypes.object,
  customer: PropTypes.object
};

const mapStateToProps = (state, initialProps) => {
  const {context} = initialProps;
  const {delivery, payment, orderItem} = context.state;

  return {
    errors: getOperationError(state, 'customerDao', 'checkout'),
    orderItem,
    delivery,
    payment,
    busy: isAnyOperationPending(state, { customerDao: 'checkout'})  || isAnyLoading(state, ['orderItemsDao']),
    ...initialProps
  };
};
export default connect(
  mapStateToProps
)(OrderConfirmation);

