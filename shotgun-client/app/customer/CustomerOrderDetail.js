import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Container, Header, Left, Button, Body, Title, Content, Text, Grid, Col} from 'native-base';
import {CurrencyInput, OrderSummary, Icon, LoadingScreen, PriceSummary, RatingSummary, SpinnerButton, ErrorRegion, AverageRating} from 'common/components';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors} from 'common/dao';
import {cancelOrder, rejectPartner, updateOrderPrice} from 'customer/actions/CustomerActions';
import {Image} from 'react-native';
import * as ContentTypes from 'common/constants/ContentTypes';

const hasStarted = status => {
  return !~[OrderStatuses.PLACED, OrderStatuses.ACCEPTED].indexOf(status);
};

/*eslint-disable */
const staticPriceControl = (props) => <PriceSummary {...props}/>;
const dynamicPriceControl = ({price,orderSummary={},onValueChanged, ...props}) => {
  const {userId} =props;
  return hasStarted(orderSummary.status) || orderSummary.customerUserId != userId ? 
  <PriceSummary price={orderSummary.totalPrice}  {...props}/> :  
  <Col>
    <PriceSummary price={orderSummary.totalPrice} onValueChanged={onValueChanged} {...props}/>
    <CurrencyInput style={styles.input} {...props} placeholder="Enter Fixed Price" initialPrice={price} onValueChanged={onValueChanged}/>
  </Col>;
}
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', () => 'Order Summary').
    delivery(() => 'Delivery Job').
    personell(({product}) => `${product.name} Job`).
    rubbish(() => 'Rubbish Collection').
  property('RejectButtonCaption', 'Reject').
    delivery('Reject Partner').
    personell('Reject Worker').
    rubbish('Reject Partner').
  property('PricingControl', staticPriceControl).
    personell(dynamicPriceControl).
  property('TrackButtonCaption', 'Track').
    delivery('Track Partner').
    personell('Track Worker').
    rubbish('Track Partner');
    /*eslint-disable */


class CustomerOrderDetail extends Component{
  constructor(props) {
    super(props);
    this.onFixedPriceValueChanged = this.onFixedPriceValueChanged.bind(this);
    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);
  }

  componentDidMount(){
    this.subscribeToOrderSummary(this.props);
  }

  componentWillReceiveProps(newProps){
    ContentTypes.resolveResourceFromProps(newProps, resourceDictionary, this);
    this.subscribeToOrderSummary(newProps);
  }

  subscribeToOrderSummary(props){
    const {dispatch, orderId, orderSummary} = props;
    if (orderSummary == undefined) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        reportId: 'customerOrderSummary'
      }));
    }
  }

  onFixedPriceValueChanged(newPrice){
    const {orderSummary, dispatch} = this.props;
    dispatch(updateOrderPrice(orderSummary.orderId, newPrice));
  }

  render() {
    const {orderSummary = {status: ''}, client, history, busy, busyUpdating, dispatch, errors, parentPath, userId, ordersPath} = this.props;
    const {delivery = {}} = orderSummary;
    const isCancelled = orderSummary.status == OrderStatuses.CANCELLED;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;
    const hasPartner = delivery.partnerFirstName !== undefined;
    const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
    const showCancelButton = !isComplete && !isCancelled && !hasPartner;
    const showRejectPartnerButton = hasPartner && !isComplete && !isOnRoute;
    const {resources} = this;
    const {PricingControl} = resources;

    const onCancelOrder = () => {
      dispatch(cancelOrder(orderSummary.orderId, () => history.push({pathname: `${ordersPath}`, transition: 'right'})));
    };

    const onRejectPartner = () => {
      dispatch(rejectPartner(orderSummary.orderId, () => history.push({pathname: `${ordersPath}`, transition: 'right'})));
    };

    const onPressTrack = () => {
      history.push({pathname: `${parentPath}/CustomerOrderInProgress`, transition: 'left'}, {orderId: orderSummary.orderId});
    }

    return busy ? <LoadingScreen text="Loading Order"/> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{resources.PageTitle(orderSummary)}</Title></Body>
      </Header>
      <Content>
        <ErrorRegion errors={errors}/>
        <PricingControl readonly={busyUpdating} userId={userId} onValueChanged={this.onFixedPriceValueChanged} isFixedPrice={delivery.isFixedPrice} orderStatus={orderSummary.status} isPartner={false} orderSummary={orderSummary} price={orderSummary.totalPrice}/>
        {showCancelButton ? <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onCancelOrder}><Text uppercase={false}>Cancel</Text></SpinnerButton> : null}
        {hasPartner && !isComplete ? <Grid style={styles.partnerDetailView}>
          <Col style={{alignItems: 'flex-end'}}>
            <Image source={{uri: delivery.partnerImageUrl}} resizeMode='contain' style={styles.partnerImage}/>
          </Col>
          <Col>
            <Text>{delivery.partnerFirstName} {delivery.partnerLastName}</Text>
            <AverageRating rating={delivery.partnerRatingAvg}/>
          </Col>
        </Grid> : null}
        {showRejectPartnerButton ? <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onRejectPartner}><Text uppercase={false}>{resources.RejectButtonCaption}</Text></SpinnerButton> : null}
        {isOnRoute ? <Button padded fullWidth style={styles.ctaButton} signOutButton onPress={onPressTrack}><Text uppercase={false}>{resources.TrackButtonCaption}</Text></Button> : null}
        <RatingSummary orderSummary={orderSummary} isPartner={false}/>
        <OrderSummary delivery={orderSummary.delivery} orderItem={orderSummary.orderItem} client={client} product={orderSummary.product} contentType={orderSummary.contentType}/>
      </Content>
    </Container>;
  }
}

const styles = {
  ctaButton: {
    marginTop: 10
  },
  partnerDetailView: {
    marginTop: 15,
    flex: 1,
    alignContent: 'center',
    alignItems: 'center'
  },
  partnerImage: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 40,
    marginRight: 10
  },
  input: {
    paddingHorizontal: 8,
    backgroundColor: '#FFFFFF',
    borderRadius: 4,
    padding: 10,
    width: 140,
    alignSelf: 'center',
    marginLeft: 20,
    borderWidth: 0.5,
    borderColor: '#edeaea'
  }
};


const findOrderSummaryFromDao = (state, orderId, daoName) => {
  const orderSummaries = getDaoState(state, ['orders'], daoName) || [];
  return  orderSummaries.find(o => o.orderId == orderId);
}

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  let orderSummary = findOrderSummaryFromDao(state,orderId,'orderSummaryDao');
  orderSummary = orderSummary || findOrderSummaryFromDao(state,orderId,'singleOrderSummaryDao');
  
  const {contentType: selectedContentType} = (orderSummary || {});
  const errors = getOperationErrors(state, [{customerDao: 'cancelOrder'}, {customerDao: 'rejectPartner'}, {customerDao: 'updateOrderPrice'}])
  return {
    ...initialProps,
    selectedContentType,
    orderId,
    errors,
    busyUpdating: isAnyOperationPending(state, [{customerDao: 'cancelOrder'}, {customerDao: 'rejectPartner'}, {customerDao: 'updateOrderPrice'}]),
    busy: isAnyOperationPending(state, [{ singleOrderSummaryDao: 'resetSubscription'}]) || orderSummary == undefined,
    orderSummary
  };
};

export default connect(
  mapStateToProps
)(CustomerOrderDetail);

