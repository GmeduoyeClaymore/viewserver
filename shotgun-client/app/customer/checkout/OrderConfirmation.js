import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Spinner,  Container, Content, Header, Text, Title, Body, Left, Button, Icon} from 'native-base';
import {checkout} from 'customer/actions/CustomerActions';
import {isAnyOperationPending, getOperationError} from 'common/dao';
import ErrorRegion from 'common/components/ErrorRegion';
import OrderSummary from 'common/components/OrderSummary';
import PriceSummary from 'common/components/PriceSummary';
import {OrderStatuses} from 'common/constants/OrderStatuses';

class OrderConfirmation extends Component{
  constructor(props){
    super(props);
    this.state = {};
    this.purchase = this.purchase.bind(this);
  }

  purchase(){
    const {dispatch, history, orderItem, payment, delivery, selectedProduct} = this.props;
    dispatch(checkout(orderItem, payment, delivery, selectedProduct, () => history.push('/Customer/CustomerOrders')));
  }

  async componentWillMount(){
    const {loadEstimatedPrice} = this.props;
    const price  = await loadEstimatedPrice();
    this.setState({price});
  }

  async componentWillReceiveProps(){
    const {loadEstimatedPrice} = this.props;
    const price  = await loadEstimatedPrice();
    this.setState({price});
  }

  render(){
    const {client, navigationStrategy, errors, busy, orderItem, delivery, selectedProduct, selectedContentType} = this.props;
    const {price} = this.state;
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
        <PriceSummary orderStatus={OrderStatuses.PLACED} isDriver={false} price={price}/>
        <OrderSummary delivery={delivery} orderItem={orderItem} client={client} product={selectedProduct} contentType={selectedContentType}/>
        <ErrorRegion errors={errors}>
          {!busy ? <Button onPress={this.purchase} fullWidth iconRight paddedBottom><Text uppercase={false}>Create Job</Text><Icon name='arrow-forward'/></Button> :  <Spinner />}
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
  const {context, client} = initialProps;
  const {delivery, payment, orderItem, selectedProduct, estimatedTotalPrice, selectedContentType} = context.state;
  const loadEstimatedPrice = async () => {
    const {estimatedTotalPrice: savedTotalPrice} = context.state;
    if (savedTotalPrice){
      return savedTotalPrice;
    }
    const estimatedTotalPrice = await client.invokeJSONCommand('orderController', 'calculateTotalPrice', {orderItems: [orderItem], payment, delivery});
    context.setState({estimatedTotalPrice});
    return estimatedTotalPrice;
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

