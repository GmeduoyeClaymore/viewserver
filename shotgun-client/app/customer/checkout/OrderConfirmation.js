import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'custom-redux';
import {Container, Content, Header, Text, Title, Body, Left, Button} from 'native-base';
import {checkout} from 'customer/actions/CustomerActions';
import {isAnyOperationPending, getOperationError} from 'common/dao';
import {OrderSummary, PriceSummary, SpinnerButton, Icon, ErrorRegion} from 'common/components';
import {OrderStatuses} from 'common/constants/OrderStatuses';

class OrderConfirmation extends Component{
  constructor(props){
    super(props);
    this.state = {};
    this.purchase = this.purchase.bind(this);
    this.loadEstimatedPrice = this.loadEstimatedPrice.bind(this);
  }

  purchase(){
    const {dispatch, history, orderItem, payment, delivery, selectedProduct} = this.props;
    dispatch(checkout(orderItem, payment, delivery, selectedProduct, () => history.push('/Customer/CustomerOrders')));
  }

  async componentWillMount(){
    await this.loadEstimatedPrice();
  }

  async loadEstimatedPrice(){
    const {client, orderItem, payment, delivery} = this.props;
    const  price = await client.invokeJSONCommand('orderController', 'calculateTotalPrice', {orderItems: [orderItem], payment, delivery});
    this.setState({price});
  }

  render(){
    const {client, navigationStrategy, errors, busy, orderItem, delivery, selectedProduct, selectedContentType} = this.props;
    const {price} = this.state;

    return <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='back-arrow' onPress={() => navigationStrategy.prev()} />
          </Button>
        </Left>
        <Body><Title>Order Summary</Title></Body>
      </Header>
      <Content>
        <PriceSummary orderStatus={OrderStatuses.PLACED} isDriver={false} price={price}/>
        <OrderSummary delivery={delivery} orderItem={orderItem} client={client} product={selectedProduct} contentType={selectedContentType}/>
        <ErrorRegion errors={errors}>
          <SpinnerButton busy={busy} onPress={this.purchase} fullWidth iconRight paddedBottom><Text uppercase={false}>Create Job</Text><Icon next name='forward-arrow'/></SpinnerButton>
        </ErrorRegion>
      </Content>
    </Container>;
  }
}

OrderConfirmation.PropTypes = {
  orderItem: PropTypes.object,
  delivery: PropTypes.object,
  customer: PropTypes.object
};

const mapStateToProps = (state, initialProps) => {
  const {context} = initialProps;
  const {delivery, payment, orderItem, selectedProduct, selectedContentType} = context.state;

  return {
    ...initialProps,
    orderItem,
    delivery,
    selectedProduct,
    selectedContentType,
    payment,
    errors: getOperationError(state, 'customerDao', 'checkout'),
    busy: isAnyOperationPending(state, [{ customerDao: 'checkout'}])
  };
};

export default connect(
  mapStateToProps
)(OrderConfirmation);

