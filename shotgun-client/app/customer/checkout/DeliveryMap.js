import React, {Component} from 'react';
import {Dimensions} from 'react-native';
import Products from 'common/constants/Products';
import {connect} from 'react-redux';
import {Container, Button, Text} from 'native-base';
import {merge, assign} from 'lodash';
import MapView from 'react-native-maps';
import Logger from 'common/Logger';
import LoadingScreen from 'common/components/LoadingScreen';
import GooglePlacesInput from 'common/components/maps/GooglePlacesInput';
import AddressMarker from 'common/components/maps/AddressMarker';
import MapViewDirections from 'react-native-maps-directions';
import { withRouter } from 'react-router';
import MapService from 'common/services/MapService';

const API_KEY = 'AIzaSyBAW_qDo2aiu-AGQ_Ka0ZQXsDvF7lr9p3M';
const { width, height } = Dimensions.get('window');
const ASPECT_RATIO = width / height;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;
const EMPTY_LOCATION =  {
  flatNumber: undefined,
  line1: undefined,
  city: undefined,
  postCode: undefined,
  googlePlaceId: undefined,
  latitude: undefined,
  longitude: undefined
};


class DeliveryMap extends Component {
  constructor(props) {
    super(props);
    this.onGetCurrentPositionSuccess = this.onGetCurrentPositionSuccess.bind(this);

    this.state = {
      busy: true,
      region: {
        latitude: 0,
        longitude: 0,
        latitudeDelta: LATITUDE_DELTA,
        longitudeDelta: LONGITUDE_DELTA
      }
    };
  }

  componentWillMount(){
    try {
      navigator.geolocation.getCurrentPosition(this.onGetCurrentPositionSuccess, undefined, {enableHighAccuracy: true});
    } catch (ex){
      Logger.warning('Error when accessing user location');
    }
  }

  onGetCurrentPositionSuccess(position){
    const {latitude, longitude} = position.coords;
    Logger.info(`Got users position at ${latitude}, ${longitude}`);
    this.setState({busy: false, region: merge(this.state.region, {latitude, longitude})});
  }


  render() {
    const {history, context} = this.props;
    const {region, busy} = this.state;
    const {delivery, order} = context.state;
    const {origin, destination} = delivery;

    const showDirections = origin.googlePlaceId !== undefined && destination.googlePlaceId !== undefined;
    const showDoneButton = origin.googlePlaceId !== undefined && (order.productId == Products.DISPOSAL || destination.googlePlaceId);
    const showDestinationInput = order.productId == Products.DELIVERY;

    const onLocationSelect = (type, details) => {
      const {delivery} = this.props.context.state;
      const newLocation = MapService.parseGooglePlacesData(details);
      Logger.info(`Setting location to ${JSON.stringify(newLocation)}`);

      context.setState({delivery: merge({}, delivery, {[type]: newLocation})});
      updateMapRegion(newLocation.latitude, newLocation.longitude);
    };

    const onChangeText = (type, text) => {
      if (text == undefined || text == '') {
        clearLocation(type);
      }
    };

    const clearLocation = (type) => {
      context.setState({delivery: assign({}, delivery, {[type]: EMPTY_LOCATION})});
    };

    const updateMapRegion = (latitude, longitude) => {
      this.map.animateToCoordinate({latitude, longitude}, 1);
    };

    const closeInputs = () => {
      this.originInput.triggerBlur();

      if (showDestinationInput) {
        this.destinationInput.triggerBlur();
      }
    };

    return busy ? <LoadingScreen text="Loading Map"/> : <Container style={{flex: 1}}>

      <MapView ref={c => {this.map = c;}} style={styles.map} showsUserLocation={true} showsMyLocationButton={true} initialRegion={region} onPress={closeInputs}>
        {showDirections ? <MapViewDirections origin={origin.location} destination={destination.location} apikey={API_KEY} strokeWidth={3} onReady={(result) => {
          this.map.fitToCoordinates(result.coordinates,  {
            edgePadding: {
              right: Math.round(width / 20) + 100,
              bottom: Math.round(height / 20),
              left: Math.round(width / 20) + 100,
              top: Math.round(height / 20) + 300,
            }});
        }}/> : null}

        {origin.googlePlaceId ? <MapView.Marker coordinate={{...origin}}>
          <AddressMarker address={origin.line1} />
        </MapView.Marker> : null}

        {destination.googlePlaceId ? <MapView.Marker coordinate={{...destination}}>
          <AddressMarker address={destination.line1} />
        </MapView.Marker> : null}
      </MapView>

      {showDestinationInput ? <GooglePlacesInput ref={c => {this.destinationInput = c;}} apiKey={API_KEY} onChangeText={(text) => onChangeText('destination', text)} onSelect={details => onLocationSelect('destination', details)} style={styles.destinationInput} placeholder='Drop-off Location'/> : null}
      <GooglePlacesInput ref={c => {this.originInput = c;}} apiKey={API_KEY} onChangeText={(text) => onChangeText('origin', text)}  onSelect={details => onLocationSelect('origin', details)} style={styles.originInput} placeholder='Pick-up Location'/>
      {showDoneButton ? <Button onPress={() => history.push('/Customer/Checkout/DeliveryOptions')} style={styles.doneButton}><Text>Done</Text></Button> : null}
    </Container>;
  }
}

const styles = {
  map: {
    flex: 1
  },
  doneButton: {
    flex: 1,
    marginBottom: 2,
    marginLeft: 20,
    marginRight: 20,
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    alignItems: 'center',
    justifyContent: 'center'
  },
  originInput: {
    container: {
      flex: 1,
      height: 200,
      margin: 2,
      position: 'absolute',
      top: 0,
      left: 0,
      right: 0
    }
  },
  destinationInput: {
    container: {
      flex: 1,
      height: 200,
      margin: 2,
      position: 'absolute',
      top: 50,
      left: 0,
      right: 0
    }
  }
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(DeliveryMap));


