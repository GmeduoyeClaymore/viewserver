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

const OrderConfirmation = ({client, dispatch, history, errors, busy, orderItem, payment, delivery}) => {
  const purchase = async() => {
    dispatch(checkout(orderItem, payment, delivery, () => history.push('/Customer/Checkout/OrderComplete')));
  };

  return <Container>
    <Header withButton>
      <Left>
        <Button>
          <Icon name='arrow-back' onPress={() => history.goBack()} />
        </Button>
      </Left>
      <Body><Title>Order Summary</Title></Body>
    </Header>
    <Content>
      <PriceSummary orderStatus={OrderStatuses.PLACED} isDriver={false} price={12.00}/>
      <OrderSummary delivery={delivery} orderItem={orderItem} client={client}/>
      <ErrorRegion errors={errors}>
        {!busy ? <Button onPress={purchase} fullWidth iconRight padded><Text uppercase={false}>Create Job</Text><Icon name='arrow-forward'/></Button> :  <Spinner />}
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
  const {delivery, payment, orderItem, origin, destination} = context.state;

  return {
    ...initialProps,
    errors: getOperationError(state, 'customerDao', 'checkout'),
    orderItem,
    delivery: {...delivery, origin, destination},
    payment,
    busy: isAnyOperationPending(state, { customerDao: 'checkout'})
  };
};
export default connect(
  mapStateToProps
)(OrderConfirmation);

