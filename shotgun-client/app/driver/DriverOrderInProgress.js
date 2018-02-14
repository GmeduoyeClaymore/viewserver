import React, {Component} from 'react';
import {Dimensions, Linking} from 'react-native';
import {connect} from 'custom-redux';
import {Container, Button, Text, Grid, Col, Row} from 'native-base';
import MapView from 'react-native-maps';
import {updateSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors} from 'common/dao';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {completeOrderRequest, stopWatchingPosition, callCustomer} from 'driver/actions/DriverActions';
import shotgun from 'native-base-theme/variables/shotgun';
import {withRouter} from 'react-router';
import {RatingAction, ErrorRegion, LoadingScreen, SpinnerButton, Icon} from 'common/components';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import Logger from 'common/Logger';

const {width, height} = Dimensions.get('window');
const ASPECT_RATIO = width / height;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

class DriverOrderInProgress extends Component{
  constructor(props) {
    super(props);

    this.state = {
      initialPosition: undefined
    };
  }

  componentWillMount(){
    const {dispatch, orderId, position} = this.props;
    dispatch(updateSubscriptionAction('orderSummaryDao', {
      orderId,
      reportId: 'driverOrderSummary'
    }));

    this.setState({initialPosition: position});
  }

  render() {
    let map;
    const {orderSummary = {status: ''}, history, position, dispatch, busy, busyUpdating, client, errors} = this.props;
    const {initialPosition} = this.state;
    const {latitude, longitude} = position;
    const {delivery = {}, contentType} = orderSummary;
    const {origin = {}, destination = {}, customerRating} = delivery;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;

    const onCompletePress = async() => {
      dispatch(completeOrderRequest(orderSummary.orderId, () => dispatch(stopWatchingPosition())));
    };

    const onPressCallCustomer = async () => {
      dispatch(callCustomer(orderSummary.orderId));
    };

    const onNavigatePress = async() => {
      const navigationDestination = destination.line1 !== undefined ? destination : origin;
      const wayPoints = destination.line1 !== undefined ? `&waypoints=${origin.latitude},${origin.longitude}` : undefined;
      const mapUrl = `https://www.google.com/maps/dir/?api=1&travelmode=driving&dir_action=navigate${wayPoints}&origin=${latitude},${longitude}&destination=${navigationDestination.latitude},${navigationDestination.longitude}`;
      try {
        const isSupported = await Linking.canOpenURL(mapUrl);

        if (isSupported) {
          return Linking.openURL(mapUrl);
        }
        throw new Error('Could not open');
      } catch (e) {
        Logger.warning(`Could not open ${mapUrl}`);
      }
    };

    const fitMap = () => {
      if ((origin.line1 !== undefined && destination.line1 !== undefined)) {
        map.fitToElements(false);
      }
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
          <ErrorRegion errors={errors}/>
          <MapView ref={c => { map = c; }} style={{ flex: 1 }} onMapReady={fitMap} initialRegion={region}
            showsUserLocation={true} showsBuidlings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={true}>
            <MapViewDirections client={client} locations={[initialPosition, origin]} strokeWidth={3} strokeColor={shotgun.brandSecondary}/>
            {contentType.destination ? <MapViewDirections client={client} locations={[origin, destination]} strokeWidth={3} /> : null}

            <MapView.Marker coordinate={{...origin}}><AddressMarker address={origin.line1}/></MapView.Marker>
            {contentType.destination ? <MapView.Marker coordinate={{...destination}}><AddressMarker address={destination.line1}/></MapView.Marker> : null}
          </MapView>
          <Button transparent style={styles.backButton}>
            <Icon name='back-arrow' onPress={() => history.goBack()} />
          </Button>
        </Row>
        <Row size={40} style={styles.infoRow}>
          {isComplete ?
            <Col>
              <Row>
                <Col>
                  <RatingAction isDriver={true} delivery={delivery}/>
                </Col>
              </Row>
              <Row><Col style={{justifyContent: 'flex-end'}}><Button fullWidth disabled={customerRating == -1} onPress={()=> history.push('/Driver')}><Text uppercase={false}>Done</Text></Button></Col></Row>
            </Col> :
            <Col>
              <Grid>
                <Row><Icon name="pin" paddedIcon originPin/><Text>{origin.line1}, {origin.postCode}</Text></Row>
                {contentType.destination ? <Row><Icon paddedIcon name="pin"/><Text>{destination.line1}, {destination.postCode}</Text></Row> : null}
                <Row style={styles.ctaRow}>
                  <Col><Button fullWidth style={styles.navigateButton} onPress={onNavigatePress}><Text uppercase={false}>Show navigation</Text></Button></Col>
                  <Col><Button fullWidth callButton onPress={onPressCallCustomer}><Icon name="phone"/><Text uppercase={false}>Call customer</Text></Button></Col>
                </Row>
              </Grid>
              <ErrorRegion errors={errors}>
                <SpinnerButton busy={busyUpdating} fullWidth><Text uppercase={false} onPress={onCompletePress}>Complete job</Text></SpinnerButton>
              </ErrorRegion>
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
  ctaRow: {
    paddingBottom: 15
  },
  navigateButton: {
    borderTopRightRadius: 0,
    borderBottomRightRadius: 0,
  }
};

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  const orderSummaries = getDaoState(state, ['orders'], 'orderSummaryDao') || [];
  const orderSummary = orderSummaries.find(o => o.orderId == orderId);
  const position = getDaoState(state, ['position'], 'userDao');
  const errors = getOperationErrors(state, [{driverDao: 'startOrderRequest'}, {driverDao: 'completeOrderRequest'}, {driverDao: 'callCustomer'}, { orderSummaryDao: 'updateSubscription'}, {userDao: 'getCurrentPosition'}]);

  return {
    ...initialProps,
    position,
    orderId,
    errors,
    busyUpdating: isAnyOperationPending(state, [{driverDao: 'startOrderRequest'}, {driverDao: 'completeOrderRequest'}]),
    busy: isAnyOperationPending(state, [{ orderSummaryDao: 'updateSubscription'}, {userDao: 'getCurrentPosition'}]) || orderSummary == undefined  || !position,
    orderSummary
  };
};

mapStateToProps.dependsOnOwnProps = true;

export default withRouter(connect(
  mapStateToProps
)(DriverOrderInProgress));

