import React, {Component} from 'react';
import {Dimensions} from 'react-native';
import {connect} from 'react-redux';
import {Container, Button, Text, Icon, Grid, Col, Row} from 'native-base';
import MapView from 'react-native-maps';
import {updateSubscriptionAction, getDaoState, isAnyOperationPending} from 'common/dao';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {completeOrderRequest} from 'driver/actions/DriverActions';
import Products from 'common/constants/Products';
import shotgun from 'native-base-theme/variables/shotgun';
import {withRouter} from 'react-router';
import LoadingScreen from 'common/components/LoadingScreen';

const {width, height} = Dimensions.get('window');
const ASPECT_RATIO = width / height;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

class DriverOrderInProgress extends Component{
  constructor(props) {
    super(props);
  }

  componentWillMount(){
    const {dispatch, orderId} = this.props;
    dispatch(updateSubscriptionAction('orderSummaryDao', {
      userId: undefined,
      isCompleted: undefined,
      orderId,
      reportId: 'driverOrderSummary'
    }));
  }

  render() {
    const {orderSummary = {status: ''}, history, position, dispatch, busy} = this.props;
    const {orderItem, delivery} = orderSummary;
    const {origin, destination} = delivery;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;
    const isDelivery = orderItem.productId == Products.DELIVERY;

    const onCompletePress = async() => {
      dispatch(completeOrderRequest(orderSummary.orderId));
    };

    const region = {
      latitude: position.latitude,
      longitude: position.longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

    return busy ? <LoadingScreen text="Loading Order"/> : <Container style={{flex: 1}}>
      <Grid>
        <Row size={60}>
          <MapView ref={c => {this.map = c;}} style={{flex: 1}} showsUserLocation={true} showsMyLocationButton={true} initialRegion={region}>
            {origin.line1 ? <MapView.Marker coordinate={{...origin}}><AddressMarker address={origin.line1}/></MapView.Marker> : null}
            {isDelivery ? <MapView.Marker coordinate={{...destination}}><AddressMarker address={destination.line1}/></MapView.Marker> : null}
          </MapView>
          <Button transparent style={styles.backButton}>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Row>
        <Row size={40} style={styles.infoRow}>
          {isComplete ?
            <Col>
              <Text>Rate this customer</Text>
            </Col> :
            <Col>
              <Grid>
                <Row><Icon name="pin" paddedIcon originPin/><Text>{origin.line1}, {origin.postCode}</Text></Row>
                {isDelivery ? <Row><Icon paddedIcon name="pin"/><Text>{destination.line1}, {destination.postCode}</Text></Row> : null}
              </Grid>
              <Button fullWidth style={styles.callButton}><Text uppercase={false}>Call customer</Text></Button>
              <Button fullWidth><Text uppercase={false} onPress={onCompletePress}>Complete job</Text></Button>
            </Col>
          }
        </Row>
      </Grid>
    </Container>;
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
  callButton: {
    backgroundColor: shotgun.brandPrimary,
    borderWidth: 1,
    borderColor: shotgun.silver,
    marginBottom: 15
  }
};

const mapStateToProps = (state, initialProps = {}) => {
  const orderId = initialProps.location && initialProps.location.state ? initialProps.location.state.orderId : undefined;
  const orderSummaries = getDaoState(state, ['orders'], 'orderSummaryDao') || [];
  const orderSummary = orderSummaries.find(o => o.orderId == orderId);

  return {
    ...initialProps,
    position: getDaoState(state, ['position'], 'userDao'),
    orderId,
    busy: isAnyOperationPending(state, { orderSummaryDao: 'updateSubscription'}) || orderSummary == undefined,
    orderSummary
  };
};

export default withRouter(connect(
  mapStateToProps
)(DriverOrderInProgress));

