import React, {Component} from 'react';
import {connect} from 'react-redux';
import {Container, Header, Left, Button, Icon, Body, Title, Content, Text} from 'native-base';
import OrderSummary from 'common/components/OrderSummary';
import PriceSummary from 'common/components/PriceSummary';
import {acceptOrderRequest} from 'driver/actions/DriverActions';

class DriverOrderRequestDetail extends Component{
  constructor(props) {
    super(props);
  }

  render() {
    const {location, client, history, dispatch} = this.props;
    const {state = {}} = location;
    const {orderSummary} = state;

    const onAcceptPress = async() => {
      dispatch(acceptOrderRequest(orderSummary.orderId, () => history.push('/Driver')));
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
        <PriceSummary orderStatus={orderSummary.status} isDriver={true} price={12.00}/>
        <Button fullWidth padded style={styles.acceptButton} onPress={onAcceptPress}><Text uppercase={false}>Accept this job</Text></Button>
        <OrderSummary delivery={orderSummary.delivery} orderItem={orderSummary.orderItem} client={client}/>
      </Content>
    </Container>;
  }
}

const styles = {
  acceptButton: {
    marginTop: 20,
    marginBottom: 10
  }
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps
});

export default connect(
  mapStateToProps
)(DriverOrderRequestDetail);

