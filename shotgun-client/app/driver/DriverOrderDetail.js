import React, {Component} from 'react';
import {connect} from 'react-redux';
import {Container, Header, Left, Button, Icon, Body, Title, Content, Text} from 'native-base';
import OrderSummary from 'common/components/OrderSummary';
import {startOrderRequest, cancelOrderRequest} from 'driver/actions/DriverActions';

class DriverOrderDetail extends Component{
  constructor(props) {
    super(props);
  }

  render() {
    const {location, client, history, dispatch} = this.props;
    const {state = {}} = location;
    const {orderSummary} = state;

    const onStartPress = async() => {
      dispatch(startOrderRequest(orderSummary.orderId, () => history.push('/Driver/DriverOrderInProgress')));
    };

    const onCancelPress = async() => {
      dispatch(cancelOrderRequest(orderSummary.orderId, () => history.push('/Driver')));
    };

    return <Container>
      <Header>
        <Left>
          <Button>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>Order Summary</Title></Body>
      </Header>
      <Content>
        <Button onPress={onStartPress}><Text>Start this job</Text></Button>
        <Button onPress={onCancelPress}><Text>Cancel this job</Text></Button>
        <OrderSummary delivery={orderSummary.delivery} orderItem={orderSummary.orderItem} client={client}/>
      </Content>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => ({
  ...initialProps
});

export default connect(
  mapStateToProps
)(DriverOrderDetail);

