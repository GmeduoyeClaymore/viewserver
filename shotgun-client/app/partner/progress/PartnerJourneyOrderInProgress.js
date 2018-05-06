import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Linking, Button, Text, View} from 'native-base';
import {SpinnerButton, OriginDestinationSummary} from 'common/components';
import {getDaoState, isAnyOperationPending} from 'common/dao';
import {startJourney, completeJourney} from 'partner/actions/PartnerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import MapView from 'react-native-maps';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import AddressMarker from 'common/components/maps/AddressMarker';

const CAN_START_JOURNEY_STATUSES = ['PENDINGSTART'];
const CAN_COMPLETE_JOURNEY_STATUSES = ['ENROUTE'];

const ASPECT_RATIO = shotgun.deviceWidth / shotgun.deviceHeight;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

class PartnerJourneyOrderInProgress extends Component{
  constructor(props){
    super(props);
  }

  onNavigatePress = async() => {
    const {order} = this.props;
    const {origin, destination} = order;
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

  fitMap = () => {
    const {order} = this.props;
    const {origin, destination} = order;
    if ((origin.line1 !== undefined && destination.line1 !== undefined)) {
      map.fitToElements(false);
    }
  };

  onJourneyStart = async() => {
    const {order, dispatch} = this.props;
    dispatch(startJourney(order.orderId));
  };

  onJourneyComplete = async() => {
    const {order, dispatch} = this.props;
    dispatch(completeJourney(order.orderId));
  };

  render() {
    const {order = {}, client, busyUpdating, user = {}} = this.props;
    const {origin, destination} = order;
    
    const {latitude, longitude} = user;
    const initialPosition = {latitude, longitude};

    const region = {
      latitude,
      longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

    return <View>
      <Button fullWidth padded style={styles.startButton} onPress={this.onNavigatePress}><Text uppercase={false}>Show navigation</Text></Button>
      {!!~CAN_START_JOURNEY_STATUSES.indexOf(order.journeyOrderStatus) ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.startButton} onPress={this.onJourneyStart}><Text uppercase={false}>Start Journey</Text></SpinnerButton> : null}
      {!!~CAN_COMPLETE_JOURNEY_STATUSES.indexOf(order.journeyOrderStatus) ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.completeButton} onPress={this.onJourneyComplete}><Text uppercase={false}>Complete Journey</Text></SpinnerButton> : null}
      <OriginDestinationSummary order={order}/>
      <MapView ref={c => { map = c; }} style={{ flex: 1 }} onMapReady={this.fitMap} initialRegion={region}
        showsUserLocation={true} showsBuildings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={true}>
        <MapViewDirections client={client} locations={[initialPosition, origin]} strokeWidth={3} strokeColor={shotgun.brandSecondary}/>
        {contentType.destination ? <MapViewDirections client={client} locations={[origin, destination]} strokeWidth={3} /> : null}
        <MapView.Marker coordinate={{...origin}}><AddressMarker address={origin.line1}/></MapView.Marker>
        {contentType.destination ? <MapView.Marker coordinate={{...destination}}><AddressMarker address={destination.line1}/></MapView.Marker> : null}
      </MapView>
    </View>;
  }
}

const styles = {
  startButton: {
    marginTop: shotgun.contentPadding,
    marginBottom: 15
  }
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps,
    user: getDaoState(state, ['user'], 'userDao'),
    busyUpdating: isAnyOperationPending(state, [{ orderDao: 'startJourney'}, { orderDao: 'completeJourney'}])
  };
};

export default connect(mapStateToProps)(PartnerJourneyOrderInProgress);

