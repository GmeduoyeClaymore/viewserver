import React, {Component} from 'react';
import {withExternalState} from 'custom-redux';
import {Container, Content, Header, Text, Title, Body, Left, Button} from 'native-base';
import {checkout} from 'customer/actions/CustomerActions';
import {isAnyOperationPending, getOperationError} from 'common/dao';
import {OrderSummary, SpinnerButton, Icon, ErrorRegion} from 'common/components';
import CustomerPriceSummary from 'customer/orders/CustomerPriceSummary';
import * as ContentTypes from 'common/constants/ContentTypes';
import {OrderStatuses} from 'common/constants/OrderStatuses';

class OrderConfirmation extends Component{
  constructor(props){
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  createJob = () => {
    const {dispatch, history, order, payment, ordersPath} = this.props;
    dispatch(checkout(order, payment, () => history.replace({pathname: `${ordersPath}`, transition: 'left'}, {isCustomer: true})));
  }

  render(){
    const {client, errors, busy, order, history} = this.props;

    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{order.title}</Title></Body>
      </Header>
      <Content>
        <CustomerPriceSummary order={{...order, orderStatus: OrderStatuses.PLACED}}/>
        <OrderSummary userCreatedThisOrder={true} order={order} client={client}/>
      </Content>
      <ErrorRegion errors={errors}/>
      <SpinnerButton busy={busy} onPress={this.createJob} fullWidth iconRight paddedBottomLeftRight><Text uppercase={false}>{this.resources.SubmitButtonCaption}</Text><Icon next name='forward-arrow'/></SpinnerButton>
    </Container>;
  }
}

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
property('PageTitle', ({product= {}}) => `${product.name} Job`).
  delivery(() => 'Delivery').
  personell(({product = {}}) => `${product.name} Job`).
  rubbish(() => 'Rubbish Collection').
property('SubmitButtonCaption', 'Create Job').
  delivery('Create Delivery').
  personell('Create Job');
/*eslint-enable */

const mapStateToProps = (state, initialProps) => {
  const {justFriends, order, ...rest} = initialProps;
  return {
    ...rest,
    order: {...order, justForFriends: justFriends},
    errors: getOperationError(state, 'orderDao', 'createOrder'),
    busy: isAnyOperationPending(state, [{ orderDao: 'createOrder'}])
  };
};

export default withExternalState(mapStateToProps)(OrderConfirmation);

