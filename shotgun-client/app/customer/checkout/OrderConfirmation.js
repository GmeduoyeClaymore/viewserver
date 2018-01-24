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

const OrderConfirmation = ({client, dispatch, history, navigationStrategy, errors, busy, orderItem, payment, delivery, loadEstimatedPrice, estimatedTotalPrice, selectedProduct, selectedContentType}) => {
  const purchase = async() => {
    dispatch(checkout(orderItem, payment, delivery, selectedProduct, () => history.push('/Customer/CustomerOrders')));
  };

  loadEstimatedPrice();

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
      <PriceSummary orderStatus={OrderStatuses.PLACED} isDriver={false} price={estimatedTotalPrice}/>
      <OrderSummary delivery={delivery} orderItem={orderItem} client={client} product={selectedProduct} contentType={selectedContentType}/>
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
  const {delivery, payment, orderItem, selectedProduct, estimatedTotalPrice, selectedContentType} = context.state;
  const loadEstimatedPrice = async () => {
    const {estimatedTotalPrice: savedTotalPrice} = context.state;
    if (savedTotalPrice){
      return savedTotalPrice;
    }
    const estimatedTotalPrice = client.invokeJSONCommand('orderController', 'calculateTotalPrice', {orderItem, payment, delivery});
    context.setState({estimatedTotalPrice});
  };
  return {
    ...initialProps,
    estimatedTotalPrice,
    loadEstimatedPrice,
    errors: getOperationError(state, 'customerDao', 'checkout'),
    orderItem,
    delivery,
    selectedProduct,
    selectedContentType,
    payment,
    busy: isAnyOperationPending(state, { customerDao: 'checkout'})
  };
};
export default connect(
  mapStateToProps
)(OrderConfirmation);

