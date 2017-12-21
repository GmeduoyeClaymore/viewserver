import React, {Component} from 'react';
import {connect} from 'react-redux';
import {Container, Header, Left, Button, Icon, Body, Title, Content} from 'native-base';
import OrderSummary from 'common/components/OrderSummary';

class OrderDetail extends Component{
  constructor(props) {
    super(props);
  }

  render() {
    const {location, client, history} = this.props;
    const {state = {}} = location;
    const {orderSummary} = state;

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
)(OrderDetail);

