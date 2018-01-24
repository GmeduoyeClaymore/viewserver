import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Spinner,  Container, Content, Header, Text, Title, Body, Left, Button, Icon} from 'native-base';
import {checkout} from 'customer/actions/CustomerActions';
import {isAnyOperationPending, getOperationError} from 'common/dao';
import ErrorRegion from 'common/components/ErrorRegion';
import OrderSummary from 'common/components/OrderSummary';
import PriceSummary from 'common/components/PriceSummary';
import {OrderStatuses} from 'common/constants/OrderStatuses';

const OrderConfirmation = ({client, dispatch, history, navigationStrategy, errors, busy, orderItem, payment, delivery, getEstimatedPrice}) => {
  const purchase = async() => {
    dispatch(checkout(orderItem, payment, delivery, selectedProduct, () => history.push('/Customer/CustomerOrders')));
  };

  const totalPrice = getEstimatedPrice();


  return <Container>
    <Header withButton>
      <Left>
        <Button>
          <Icon name='arrow-back' onPress={() => navigationStrategy.prev()} />
        </Button>
      </Left>
      <Body><Title>Order Summary</Title></Body>
    </Header>
    <Content>
      <PriceSummary orderStatus={OrderStatuses.PLACED} isDriver={false} price={totalPrice}/>
      <OrderSummary delivery={delivery} orderItem={orderItem} client={client} product={selectedProduct}/>
      <ErrorRegion errors={errors}>
        {!busy ? <Button onPress={purchase} fullWidth iconRight paddedBottom><Text uppercase={false}>Create Job</Text><Icon name='arrow-forward'/></Button> :  <Spinner />}
      </ErrorRegion>
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
  const {delivery, payment, orderItem, selectedProduct} = context.state;
  const getEstimatedPrice = async () => {
    const {estimatedTotalPrice: savedTotalPrice} = context.state;
    if (savedTotalPrice){
      return savedTotalPrice;
    }
    const estimatedTotalPrice = client.invokeJSONCommand('orderController', 'calculateTotalPrice', {orderItem, payment, delivery});
    context.setState({estimatedTotalPrice});
    return estimatedTotalPrice;
  };
  return {
    ...initialProps,
    errors: getOperationError(state, 'customerDao', 'checkout'),
    getEstimatedPrice,
    orderItem,
    delivery,
    selectedProduct,
    payment,
    totalPrice,
    busy: isAnyOperationPending(state, { customerDao: 'checkout'})
  };
};
export default connect(
  mapStateToProps
)(OrderConfirmation);

