import React, {Component} from 'react';
import {Dimensions} from 'react-native';
import {connect} from 'react-redux';
import {Container, Button, Text, Icon, Grid, Col, Row} from 'native-base';
import MapView from 'react-native-maps';
import {updateSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps} from 'common/dao';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {completeOrderRequest} from 'driver/actions/DriverActions';
import Products from 'common/constants/Products';
import shotgun from 'native-base-theme/variables/shotgun';
import {withRouter} from 'react-router';
import LoadingScreen from 'common/components/LoadingScreen';
import RatingAction from 'common/components/RatingAction';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import {Linking} from 'react-native';
import Logger from 'common/Logger';

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
      isCompleted: '',
      orderId,
      reportId: 'driverOrderSummary'
    }));
  }

  render() {
    let map;
    const {orderSummary = {status: ''}, history, position, dispatch, busy, client} = this.props;
    const {latitude, longitude} = position;
    const {orderItem = {}, delivery = {}} = orderSummary;
    const {origin = {}, destination = {}, customerRating} = delivery;
    const showDirections = origin.line1 !== undefined && destination.line1 !== undefined;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;
    const isDelivery = orderItem.productId == Products.DELIVERY;

    const onCompletePress = async() => {
      dispatch(completeOrderRequest(orderSummary.orderId));
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
        throw 'Could not open';
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
          <MapView ref={c => { map = c; }} style={{ flex: 1 }} onMapReady={fitMap} initialRegion={region}
            showsUserLocation={true} showsBuidlings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={true}>
            <MapViewDirections client={client} origin={{ ...position }}
              destination={{ ...origin }} strokeWidth={3} strokeColor={shotgun.brandSecondary}/>
            {showDirections ?
              <MapViewDirections client={client} origin={{ ...origin }}
                destination={{ ...destination }} strokeWidth={3} /> : null}
            <MapView.Marker coordinate={{...position}}/>
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
              <Row>
                <Col>
                  <RatingAction isDriver={true} delivery={delivery}/>
                </Col>
              </Row>
              <Row><Col style={{justifyContent: 'flex-end'}}><Button fullWidth disabled={customerRating == 0} onPress={()=> history.push('/Driver')}><Text uppercase={false}>Done</Text></Button></Col></Row>
            </Col> :
            <Col>
              <Grid>
                <Row><Icon name="pin" paddedIcon originPin/><Text>{origin.line1}, {origin.postCode}</Text></Row>
                {isDelivery ? <Row><Icon paddedIcon name="pin"/><Text>{destination.line1}, {destination.postCode}</Text></Row> : null}
                <Row style={styles.ctaRow}>
                  <Col><Button fullWidth style={styles.navigateButton} onPress={onNavigatePress}><Text uppercase={false}>Show navigation</Text></Button></Col>
                  <Col><Button fullWidth style={styles.callButton}><Text uppercase={false}>Call customer</Text></Button></Col>
                </Row>
              </Grid>
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
  ctaRow: {
    paddingBottom: 15
  },
  callButton: {
    backgroundColor: shotgun.brandPrimary,
    borderWidth: 1,
    borderTopLeftRadius: 0,
    borderBottomLeftRadius: 0,
    borderColor: shotgun.silver
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

  return {
    ...initialProps,
    position,
    orderId,
    busy: isAnyOperationPending(state, { orderSummaryDao: 'updateSubscription', userDao: 'getCurrentPosition'}) || orderSummary == undefined  || !position,
    orderSummary
  };
};

mapStateToProps.dependsOnOwnProps = true;

export default withRouter(connect(
  mapStateToProps
)(DriverOrderInProgress));

