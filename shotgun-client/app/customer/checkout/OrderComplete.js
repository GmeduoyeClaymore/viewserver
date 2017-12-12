import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Text, Container, Content, Header, Title, Body, Button} from 'native-base';
import {getDaoCommandResult} from 'common/dao';

const OrderComplete = ({history, orderId}) => {
  return <Container>
    <Header>
      <Body><Title>Order Complete</Title></Body>
    </Header>
    <Content>
      <Text>Your Order Has Been Placed</Text>
      <Text>{`Order Id ${orderId}`}</Text>
      <Button onPress={() => history.push('/Customer/ProductSelect')}><Text>Continue Shopping</Text></Button>
    </Content>
  </Container>;
};

OrderComplete.PropTypes = {
  order: PropTypes.object
};

const mapStateToProps = (state, initialProps) => ({
  orderId: getDaoCommandResult(state, 'checkout', 'customerDao'),
  ...initialProps
});

export default connect(
  mapStateToProps
)(OrderComplete);


