import React, {Component} from 'react';
import {Linking} from 'react-native';
import {connect} from 'custom-redux';
import {Container, Button, Text, Grid, Col, Row} from 'native-base';
import MapView from 'react-native-maps';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors} from 'common/dao';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {completeOrderRequest, stopWatchingPosition, callCustomer} from 'partner/actions/PartnerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import {RatingAction, ErrorRegion, LoadingScreen, SpinnerButton, Icon, OriginDestinationSummary} from 'common/components';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import Logger from 'common/Logger';

const ASPECT_RATIO = shotgun.deviceWidth / shotgun.deviceHeight;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

class PartnerOrderInProgress extends Component{
  constructor(props) {
    super(props);
  }

  beforeNavigateTo(){
    const {dispatch, orderId} = this.props;
    dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
      orderId,
      reportId: 'partnerOrderSummary'
    }));
  }

  render() {
    let map;
    const {orderSummary = {status: ''}, history, user, dispatch, busy, busyUpdating, client, errors, ordersRoot} = this.props;
    //const {initialPosition} = this.state;
    const {latitude, longitude} = user;
    const initialPosition = {latitude, longitude};
    const {delivery = {}, contentType, customerRating} = orderSummary;
    const {origin = {}, destination = {}} = delivery;
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

    const onRatingDonePress = () => {
      history.push({pathname: ordersRoot, transition: 'left'});
    };

    const fitMap = () => {
      if ((origin.line1 !== undefined && destination.line1 !== undefined)) {
        map.fitToElements(false);
      }
    };

    const region = {
      latitude,
      longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

    return busy ? <LoadingScreen text="Loading Order"/> : <Container style={{flex: 1}}>
      <Grid>
        <Row size={60}>
          <MapView ref={c => { map = c; }} style={{ flex: 1 }} onMapReady={fitMap} initialRegion={region}
            showsUserLocation={true} showsBuildings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={true}>
            <MapViewDirections client={client} locations={[initialPosition, origin]} strokeWidth={3} strokeColor={shotgun.brandSecondary}/>
            {contentType.destination ? <MapViewDirections client={client} locations={[origin, destination]} strokeWidth={3} /> : null}

            <MapView.Marker coordinate={{...origin}}><AddressMarker address={origin.line1}/></MapView.Marker>
            {contentType.destination ? <MapView.Marker coordinate={{...destination}}><AddressMarker address={destination.line1}/></MapView.Marker> : null}
          </MapView>
          <Button transparent style={styles.backButton} onPress={() => history.push({pathname: `${ordersRoot}/PartnerOrders`, transition: 'right'})}>
            <Icon name='back-arrow'/>
          </Button>
        </Row>
        <Row size={40} style={styles.infoRow}>
          {isComplete ?
            <Col>
              <Row>
                <Col>
                  <RatingAction isPartner={true} orderSummary={orderSummary}/>
                </Col>
              </Row>
              <Row><Col style={{justifyContent: 'flex-end'}}><Button fullWidth disabled={customerRating == 0} onPress={onRatingDonePress}><Text uppercase={false}>Done</Text></Button></Col></Row>
            </Col> :
            <Col>
              <Grid>
                <Row>
                  <OriginDestinationSummary contentType={contentType} delivery={delivery}/>
                </Row>
                <Row style={styles.ctaRow}>
                  <Col><Button fullWidth style={styles.navigateButton} onPress={onNavigatePress}><Text uppercase={false}>Show navigation</Text></Button></Col>
                  <Col><Button fullWidth statusButton onPress={onPressCallCustomer}><Icon name="phone"/><Text uppercase={false}>Call customer</Text></Button></Col>
                </Row>
              </Grid>
            </Col>
          }
        </Row>
      </Grid>
      <ErrorRegion errors={errors} style={styles.error} />
      {!isComplete ? <SpinnerButton busy={busyUpdating} paddedBottom fullWidth onPress={onCompletePress}><Text uppercase={false}>Complete job</Text></SpinnerButton> : null}
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
  error: {
    padding: shotgun.contentPadding
  },
  ctaRow: {
    paddingTop: 15
  },
  navigateButton: {
    borderTopRightRadius: 0,
    borderBottomRightRadius: 0,
  }
};

const findOrderSummaryFromDao = (state, orderId, daoName) => {
  const orderSummaries = getDaoState(state, ['orders'], daoName) || [];
  return  orderSummaries.find(o => o.orderId == orderId);
};


const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  let orderSummary = findOrderSummaryFromDao(state, orderId, 'orderSummaryDao');
  orderSummary = orderSummary || findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');
  const errors = getOperationErrors(state, [{partnerDao: 'startOrderRequest'}, {partnerDao: 'completeOrderRequest'}, {partnerDao: 'callCustomer'}, { orderSummaryDao: 'resetSubscription'}]);
  const user = getDaoState(state, ['user'], 'userDao');

  return {
    ...initialProps,
    user,
    orderId,
    errors,
    busyUpdating: isAnyOperationPending(state, [{partnerDao: 'startOrderRequest'}, {partnerDao: 'completeOrderRequest'}]),
    busy: isAnyOperationPending(state, [{ orderSummaryDao: 'resetSubscription'}, {userDao: 'getCurrentPosition'}]) || orderSummary == undefined  || !user.latitude,
    orderSummary
  };
};

mapStateToProps.dependsOnOwnProps = true;

export default connect(
  mapStateToProps
)(PartnerOrderInProgress);

