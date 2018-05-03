import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Container, Button} from 'native-base';
import DeliveryAndRubbishCustomerOrderInProgress from './DeliveryAndRubbishCustomerOrderInProgress';
import PersonellCustomerOrderInProgress from './PersonellCustomerOrderInProgress';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getOperationError} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {LoadingScreen, Icon} from 'common/components';
import * as ContentTypes from 'common/constants/ContentTypes';

const resourceDictionary = new ContentTypes.ResourceDictionary();
/*eslint-disable */
resourceDictionary.
  property('ProgressViewControl', DeliveryAndRubbishCustomerOrderInProgress).
    personell(PersonellCustomerOrderInProgress).
    rubbish(DeliveryAndRubbishCustomerOrderInProgress).
    delivery(DeliveryAndRubbishCustomerOrderInProgress).
  property('PageTitle', () => 'Order Summary').
    delivery(() => 'Delivery Job').
    personell(({product}) => `${product.name} Job`).
    rubbish(() => 'Rubbish Collection');


class CustomerOrderInProgress extends Component{
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  beforeNavigateTo(){
    const {dispatch, orderId} = this.props;
    dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
      orderId,
      reportId: 'customerOrderSummary'
    }));
  }

  render(){
    const {resources} = this;
    const {history, busy, orderSummary} = this.props;
    const {ProgressViewControl} = resources;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;

    busy ? <LoadingScreen text="Loading Order"/> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{resources.PageTitle(orderSummary)}</Title></Body>
      </Header>
      <Content>
        {isComplete ? [<RatingAction isPartner={false} orderSummary={orderSummary}/>,
        <Button fullWidth disabled={orderSummary.partnerRating == 0} onPress={()=> history.push(`${parentPath}/CustomerOrders`)}><Text uppercase={false}>Done</Text></Button>]: undefined}
        <ProgressViewControl {...this.props}/>
      </Content>
    </Container>
  }
}

const styles = {
  backButton: {
    position: 'absolute',
    left: 0,
    top: 0
  },
  infoRow: {
    padding: shotgun.contentPadding
  },
  data: {
    fontWeight: 'bold'
  },
  navigateButton: {
    borderTopRightRadius: 0,
    borderBottomRightRadius: 0,
  },
  images: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 60,
    marginRight: 10
  }
};

const findOrderSummaryFromDao = (state, orderId, daoName) => {
  const orderSummaries = getDaoState(state, ['orders'], daoName) || [];
  return  orderSummaries.find(o => o.orderId == orderId);
};

const mapStateToProps = (state, initialProps) => {
  const orderId = initialProps.location && initialProps.location.state ? initialProps.location.state.orderId : undefined;
  let orderSummary = findOrderSummaryFromDao(state, orderId, 'orderSummaryDao');
  orderSummary = orderSummary || findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');
  const {contentType} = orderSummary || {};
  return {
    contentType,
    ...initialProps,
    orderId,
    errors: getOperationError(state, 'customerDao', 'callPartner' ),
    busy: isAnyOperationPending(state, [{ orderSummaryDao: 'resetSubscription'}, {userDao: 'getCurrentPosition'}]) || orderSummary == undefined,
    orderSummary
  };
};

mapStateToProps.dependsOnOwnProps = true;

export default connect(
  mapStateToProps
)(CustomerOrderInProgress);

