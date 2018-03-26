import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'custom-redux';
import {Container, Content, Header, Text, Title, Body, Left, Button} from 'native-base';
import {checkout} from 'customer/actions/CustomerActions';
import {isAnyOperationPending, getOperationError, hasAnyOptionChanged} from 'common/dao';
import {OrderSummary, PriceSummary, SpinnerButton, Icon, ErrorRegion} from 'common/components';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {calculateTotalPrice} from './CheckoutUtils';

import * as ContentTypes from 'common/constants/ContentTypes';

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', ({product}) => `${product.name} Job`).
    delivery(() => 'Delivery').
    personell(({product}) => `${product.name} Job`).
    rubbish(() => 'Rubbish Collection').
  property('SubmitButtonCaption', 'Create Job').
    delivery('Create Delivery').
    personell('Create Job');
/*eslint-enable */
class OrderConfirmation extends Component{
  constructor(props){
    super(props);
    this.state = {};
    this.purchase = this.purchase.bind(this);
    this.loadEstimatedPrice = this.loadEstimatedPrice.bind(this);
    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);
  }

  purchase(){
    const {dispatch, history, orderItem, payment, delivery, selectedProduct} = this.props;
    dispatch(checkout(orderItem, payment, delivery, selectedProduct, () => history.push('/Customer/CustomerOrders')));
  }

  async componentDidMount(){
    await this.loadEstimatedPrice();
  }

  componentWillReceiveProps(newProps){
    ContentTypes.resolveResourceFromProps(newProps, resourceDictionary, this);
    if (hasAnyOptionChanged(this.props, newProps, ['isReady']) && newProps.isReady){
      this.loadEstimatedPrice();
    }
  }

  async loadEstimatedPrice(){
    const {client, orderItem, payment, delivery} = this.props;
    const  price = await calculateTotalPrice({client, delivery, orderItem});
    this.setState({price});
  }

  render(){
    const {resources} = this;
    const {client, navigationStrategy, errors, busy, orderItem, delivery, deliveryUser, selectedProduct, selectedContentType} = this.props;
    const {price} = this.state;

    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => navigationStrategy.prev()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{resources.PageTitle({product: selectedProduct})}</Title></Body>
      </Header>
      <Content>
        <PriceSummary orderStatus={OrderStatuses.PLACED} isFixedPrice={delivery.isFixedPrice} isDriver={false} price={price}/>
        <OrderSummary delivery={delivery} deliveryUser={deliveryUser} orderItem={orderItem} client={client} product={selectedProduct} contentType={selectedContentType}/>
        <ErrorRegion errors={errors}>
          <SpinnerButton busy={busy} onPress={this.purchase} fullWidth iconRight paddedBottom><Text uppercase={false}>{resources.SubmitButtonCaption}</Text><Icon next name='forward-arrow'/></SpinnerButton>
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
  const {delivery, payment, orderItem, selectedProduct, selectedContentType, deliveryUser} = context.state;

  return {
    ...initialProps,
    orderItem,
    delivery,
    deliveryUser,
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

