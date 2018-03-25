import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Container, Header, Left, Button, Body, Title, Content, Text, Grid, Col} from 'native-base';
import {OrderSummary, Icon, LoadingScreen, PriceSummary, RatingSummary, SpinnerButton} from 'common/components';
import  {CurrencyInput} from 'common/components/basic';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps} from 'common/dao';
import {cancelOrder, rejectDriver, updateOrderPrice} from 'customer/actions/CustomerActions';
import {Image} from 'react-native';
import * as ContentTypes from 'common/constants/ContentTypes';


const hasStarted = status => {
  return !~[OrderStatuses.PLACED, OrderStatuses.ACCEPTED].indexOf(status);
};

/*eslint-disable */
const staticPriceControl = (props) => <PriceSummary {...props}/>;
const dynamicPriceControl = ({price,orderSummary={},onValueChanged, ...props}) => {
  return hasStarted(orderSummary.status) ? 
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
    delivery('Reject Driver').
    personell('Reject Worker').
    rubbish('Reject Driver').
  property('PricingControl', staticPriceControl).
    personell(dynamicPriceControl).
  property('TrackButtonCaption', 'Track').
    delivery('Track Driver').
    personell('Track Worker').
    rubbish('Track Driver');
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
      dispatch(resetSubscriptionAction('orderSummaryDao', {
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
    const {orderSummary = {status: ''}, client, history, busy, busyUpdating, dispatch} = this.props;
    const {delivery = {}} = orderSummary;
    const isCancelled = orderSummary.status == OrderStatuses.CANCELLED;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;
    const hasDriver = delivery.driverFirstName !== undefined;
    const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
    const showCancelButton = !isComplete && !isCancelled && !hasDriver;
    const showRejectDriverButton = hasDriver && !isComplete && !isOnRoute;
    const {resources} = this;
    const {PricingControl} = resources;

    const onCancelOrder = () => {
      dispatch(cancelOrder(orderSummary.orderId, () => history.push('/Customer/CustomerOrders')));
    };

    const onRejectDriver = () => {
      dispatch(rejectDriver(orderSummary.orderId, () => history.push('/Customer/CustomerOrders')));
    };

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
        
        <PricingControl readonly={busyUpdating} onValueChanged={this.onFixedPriceValueChanged} isFixedPrice={delivery.isFixedPrice} orderStatus={orderSummary.status} isDriver={false} orderSummary={orderSummary} price={orderSummary.totalPrice}/>
        {showCancelButton ? <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onCancelOrder}><Text uppercase={false}>Cancel</Text></SpinnerButton> : null}
        {hasDriver && !isComplete ? <Grid style={styles.driverDetailView}>
          <Col style={{alignItems: 'flex-end'}}>
            <Image source={{uri: delivery.driverImageUrl}} resizeMode='contain' style={styles.driverImage}/>
          </Col>
          <Col>
            <Text>{delivery.driverFirstName} {delivery.driverLastName}</Text>
            <Text><Icon name='star' avgStar/>{delivery.driverRatingAvg}</Text>
          </Col>
        </Grid> : null}
        {showRejectDriverButton ? <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onRejectDriver}><Text uppercase={false}>{resources.RejectButtonCaption}</Text></SpinnerButton> : null}
        {isOnRoute ? <Button padded fullWidth style={styles.ctaButton} signOutButton onPress={() => history.push('/Customer/CustomerOrderInProgress', {orderId: orderSummary.orderId})}><Text uppercase={false}>{resourceDictionary.TrackButtonCaption}</Text></Button> : null}
        <RatingSummary orderSummary={orderSummary} isDriver={false}/>
        <OrderSummary delivery={orderSummary.delivery} orderItem={orderSummary.orderItem} client={client} product={orderSummary.product} contentType={orderSummary.contentType}/>
      </Content>
    </Container>;
  }
}

const styles = {
  ctaButton: {
    marginTop: 10
  },
  driverDetailView: {
    marginTop: 15,
    flex: 1,
    alignContent: 'center',
    alignItems: 'center'
  },
  driverImage: {
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

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  const orderSummaries = getDaoState(state, ['orders'], 'orderSummaryDao') || [];
  const orderSummary = orderSummaries.find(o => o.orderId == orderId);
  const {contentType: selectedContentType} = (orderSummary || {});
  return {
    ...initialProps,
    selectedContentType,
    orderId,
    busyUpdating: isAnyOperationPending(state, [{customerDao: 'cancelOrder'}, {customerDao: 'rejectDriver'}, {customerDao: 'updateOrderPrice'}]),
    busy: isAnyOperationPending(state, [{ orderSummaryDao: 'resetSubscription'}]) || orderSummary == undefined,
    orderSummary
  };
};

export default connect(
  mapStateToProps
)(CustomerOrderDetail);

