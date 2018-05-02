import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Container, Header, Left, Button, Body, Title, Content, Text} from 'native-base';
import {Icon, LoadingScreen, ErrorRegion, SpinnerButton, CurrencyInput} from 'common/components';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors} from 'common/dao';
import * as ContentTypes from 'common/constants/ContentTypes';
import MapDetails from './MapDetails';
import invariant from 'invariant';
import moment from 'moment';
import {cancelOrder, rejectPartner, acceptPartner, updateOrderPrice} from 'customer/actions/CustomerActions';
import shotgun from 'native-base-theme/variables/shotgun';

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', () => 'Order Summary').
    delivery(() => 'Delivery Job').
    personell(({product}) => `${product.name} Job`).
    rubbish(() => 'Rubbish Collection');
/*eslint-disable */

const  CancelOrder = ({orderId, busyUpdating}) => {
  const onCancelOrder = () => {
      dispatch(cancelOrder(orderId));
  };
  return <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onCancelOrder}><Text uppercase={false}>Cancel</Text></SpinnerButton> 
};

const  RejectPartner = ({orderId, busyUpdating}) => {
  const onRejectPartner = () => {
      dispatch(rejectPartner(orderId));
  };
  return <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onRejectPartner}><Text uppercase={false}>Reject</Text></SpinnerButton>
};

const  AcceptPartner = ({orderId, busyUpdating}) => {
  const onAcceptPartner = () => {
      dispatch(acceptPartner(orderId));
  };
  return <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onAcceptPartner}><Text uppercase={false}>Accept</Text></SpinnerButton>
};

const onOrderAmountChanged = (orderId, newAmount) => {
  dispatch(updateOrderPrice(orderId, newAmount));
}

const PartnerAcceptRejectControl = ({partnerResponses, orderId}) => {
  return partnerResponses.map(
    response  => {
      const {latitude, longitude, firstname, lastname, email, imageUrl, online, userStatus, statusMessage, ratingAvg, estimatedDate, price, partnerOrderStatus} = response;
      const stars = [...Array(ratingAvg)].map((e, i) => <Icon name='star' key={i} style={styles.star}/>);
      return <View style={styles.view}>
          <Text>{moment(estimatedDate).format('ddd Do MMMM, h:mma')}</Text>
          <Image source={{uri: imageUrl}} resizeMode='contain' style={styles.partnerImage}/>
          <Row><RejectPartner {...this.props}/><AcceptPartner  {...this.props}/></Row>
      </View>;
    }
  );
}

class CustomerOrderDetail extends Component{
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  beforeNavigateTo(){
    this.subscribeToOrderSummary(this.props);
  }

  subscribeToOrderSummary(props){
    const {dispatch, orderId, order, isPendingOrderSummarySubscription} = props;
    if (order == undefined && !isPendingOrderSummarySubscription) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        reportId: 'customerOrderSummary'
      }));
    }
  }

  setAmount = (amount) => {
    const {order} = this.props;
    onOrderAmountChanged(order.orderId,amount);
  }

  render() {
    const {busy, order, partnerResponses, errors, history, busyUpdating} = this.props;
    const {resources} = this;
    return busy ? <LoadingScreen text="Loading Order"/> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{resources.PageTitle(order)}</Title></Body>
      </Header>
      <Content>
        <ErrorRegion errors={errors}/>
        <CancelOrder orderId={order.orderId} busyUpdating={busyUpdating} />
        <PartnerAcceptRejectControl orderId={order.orderId} partnerResponses={partnerResponses} busyUpdating={busyUpdating}/>
        <Grid>
          <Row style={styles.row}><Text style={styles.heading}>You wil pay</Text></Row>
          <Row style={styles.row}>{!price ? <Spinner/> : <Currency value={price} style={styles.price}/>}</Row>
        </Grid>
        <CurrencyInput onValueChange={this.setAmount}/>
        <MapDetails order={order}/>
        <ListItem padded style={{borderBottomWidth: 0}}>
          <Grid>
            <Row><Text style={styles.itemDetailsTitle}>{this.resources.PageTitle()}</Text></Row>
            <Row><Text>{order.description}</Text></Row>
            {order.imageUrl !== undefined && order.imageUrl !== '' ?  <Row style={{justifyContent: 'center'}}><Image source={{uri: order.imageUrl}} resizeMode='contain' style={styles.image}/></Row> : null}
          </Grid>
        </ListItem>
      </Content>
    </Container>;
  }
}

const styles = {
  row: {
    justifyContent: 'center'
  },
  view: {
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    marginTop: 5
  },
  heading: {
    fontSize: 16
  },
  text: {
    marginRight: 5
  },
  star: {
    fontSize: 15,
    padding: 2,
    color: shotgun.gold,
  },
  partnerImage: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 30,
    marginRight: 10
  },
  price: {
    fontSize: 30,
    lineHeight: 34,
    fontWeight: 'bold'
  }
};

const findOrderSummaryFromDao = (state, orderId, daoName) => {
  const orderSummaries = getDaoState(state, ['orders'], daoName) || [];
  return  orderSummaries.find(o => o.orderId == orderId);
}

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  invariant(orderId, 'orderId should be specfied');
  let order = findOrderSummaryFromDao(state,orderId,'orderSummaryDao');
  order = order || findOrderSummaryFromDao(state,orderId,'singleOrderSummaryDao');

  const {partnerResponses} = order || {};
  
  const errors = getOperationErrors(state, [{customerDao: 'cancelOrder'}, {customerDao: 'rejectPartner'}, {customerDao: 'updateOrderPrice'}])
  const isPendingOrderSummarySubscription = isAnyOperationPending(state, [{ singleOrderSummaryDao: 'resetSubscription'}]);
  return {
    ...initialProps,
    order,
    partnerResponses,
    isPendingOrderSummarySubscription,
    me: getDaoState(state, ['user'], 'userDao'),
    errors,
    busyUpdating: isAnyOperationPending(state, [{customerDao: 'cancelOrder'}, {customerDao: 'rejectPartner'}, {customerDao: 'updateOrderPrice'}]),
    busy: isPendingOrderSummarySubscription,
  };
};

export default connect(
  mapStateToProps
)(CustomerOrderDetail);

