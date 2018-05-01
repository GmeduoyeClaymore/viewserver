import React, {Component} from 'react';
import {withExternalState} from 'custom-redux';
import {Container, Content, Header, Text, Title, Body, Left, Button} from 'native-base';
import {checkout} from 'customer/actions/CustomerActions';
import {isAnyOperationPending, getOperationError} from 'common/dao';
import {OrderSummary, PriceSummary, SpinnerButton, Icon, ErrorRegion} from 'common/components';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import * as ContentTypes from 'common/constants/ContentTypes';

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
    const {client, errors, busy, order, history, selectedContentType} = this.props;

    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{this.resources.PageTitle({product: order.orderProduct})}</Title></Body>
      </Header>
      <Content>
        <PriceSummary orderStatus={OrderStatuses.PLACED} isPartner={false} price={order.amount}/>
        <OrderSummary order={order} client={client} selectedContentType={selectedContentType}/>
      </Content>
      <ErrorRegion errors={errors}/>
      <SpinnerButton busy={busy} onPress={this.createJob} fullWidth iconRight paddedBottom><Text uppercase={false}>{this.resources.SubmitButtonCaption}</Text><Icon next name='forward-arrow'/></SpinnerButton>
    </Container>;
  }
}

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

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps,
    errors: getOperationError(state, 'orderDao', 'createOrder'),
    busy: isAnyOperationPending(state, [{ orderDao: 'createOrder'}])
  };
};

export default withExternalState(mapStateToProps)(OrderConfirmation);

