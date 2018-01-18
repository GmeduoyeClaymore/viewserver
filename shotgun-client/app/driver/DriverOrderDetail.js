import React, {Component} from 'react';
import {connect} from 'react-redux';
import {Container, Header, Left, Button, Icon, Body, Title, Content, Text, View} from 'native-base';
import OrderSummary from 'common/components/OrderSummary';
import {updateSubscriptionAction, getDaoState, isAnyOperationPending} from 'common/dao';
import {startOrderRequest, cancelOrderRequest} from 'driver/actions/DriverActions';
import PriceSummary from 'common/components/PriceSummary';
import RatingSummary from 'common/components/RatingSummary';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import LoadingScreen from 'common/components/LoadingScreen';

class DriverOrderDetail extends Component{
  constructor(props) {
    super(props);
  }

  componentWillMount(){
    const {dispatch, orderId, orderSummary} = this.props;
    if (orderSummary == undefined) {
      dispatch(updateSubscriptionAction('orderSummaryDao', {
        userId: undefined,
        orderId,
        isCompleted: '',
        reportId: 'driverOrderSummary'
      }));
    }
  }

  render() {
    const {orderSummary = {status: ''}, client, history, dispatch, busy} = this.props;
    const isStarted = orderSummary.status == OrderStatuses.PICKEDUP;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;

    const onStartPress = async() => {
      dispatch(startOrderRequest(orderSummary.orderId, navigateToOrderInProgress));
    };

    const navigateToOrderInProgress = () => history.push('/Driver/DriverOrderInProgress', {orderId: orderSummary.orderId});

    const onCancelPress = async() => {
      dispatch(cancelOrderRequest(orderSummary.orderId, () => history.push('/Driver')));
    };

    return busy ? <LoadingScreen text="Loading Order"/> : <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>Order Summary</Title></Body>
      </Header>
      <Content>
        <PriceSummary orderStatus={orderSummary.status} isDriver={true} price={orderSummary.totalPrice}/>
        <RatingSummary orderStatus={orderSummary.status} isDriver={true} delivery={orderSummary.delivery}/>
        {!isComplete ?
          <View>
            {isStarted ?
              <Button fullWidth padded style={styles.startButton} onPress={navigateToOrderInProgress}><Text uppercase={false}>Show navigation map</Text></Button> :
              <Button fullWidth padded style={styles.startButton} onPress={onStartPress}><Text uppercase={false}>Start this job</Text></Button>
            }
            <Button fullWidth padded cancelButton onPress={onCancelPress}><Text uppercase={false}>Cancel this job</Text></Button>
          </View> : null
        }

        <OrderSummary delivery={orderSummary.delivery} orderItem={orderSummary.orderItem} client={client}/>
      </Content>
    </Container>;
  }
}

const styles = {
  startButton: {
    marginTop: shotgun.contentPadding,
    marginBottom: 15
  }
};

const mapStateToProps = (state, initialProps) => {
  const orderId = initialProps.location.state.orderId;
  const orderSummaries = getDaoState(state, ['orders'], 'orderSummaryDao') || [];
  const orderSummary = orderSummaries.find(o => o.orderId == orderId);

  return {
    ...initialProps,
    orderId,
    busy: isAnyOperationPending(state, { orderSummaryDao: 'updateSubscription'}) || orderSummary == undefined,
    orderSummary
  };
};

export default connect(
  mapStateToProps
)(DriverOrderDetail);

