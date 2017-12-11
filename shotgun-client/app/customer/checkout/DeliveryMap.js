import React, {Component} from 'react';
import {Dimensions} from 'react-native';
import {connect} from 'react-redux';
import {Container, Button, Text} from 'native-base';
import {merge, assign} from 'lodash';
import {getDaoCommandResult} from 'common/dao';
import MapView from 'react-native-maps';
import Logger from 'common/Logger';
import LoadingScreen from 'common/components/LoadingScreen';
import GooglePlacesInput from 'common/components/maps/GooglePlacesInput';
import AddressMarker from 'common/components/maps/AddressMarker';
import MapViewDirections from 'react-native-maps-directions';
import { withRouter } from 'react-router';

const API_KEY = 'AIzaSyBAW_qDo2aiu-AGQ_Ka0ZQXsDvF7lr9p3M';
const { width, height } = Dimensions.get('window');
const ASPECT_RATIO = width / height;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;


class DeliveryMap extends Component {
  constructor(props) {
    super(props);
    this.onGetCurrentPositionSuccess = this.onGetCurrentPositionSuccess.bind(this);
    this.closeInputs = this.closeInputs.bind(this);

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

  onLocationSelect(type, details){
    const {name, place_id, geometry} = details;
    const {location} = geometry;
    const {delivery} = this.props.context.state;

    this.props.context.setState({delivery: merge({}, delivery, {[type]: {name, place_id, location: {latitude: location.lat, longitude: location.lng}}})});
    this.updateMapRegion(location.lat, location.lng);
  }

  onChangeText(type, text){
    if (text == undefined || text == '') {
      this.clearLocation(type);
    }
  }

  clearLocation(type){
    const {delivery} = this.props.context.state;
    this.props.context.setState({delivery: assign({}, delivery, {[type]: {name: undefined, place_id: undefined, location: {latitude: undefined, longitude: undefined}}})});
  }

  updateMapRegion(latitude, longitude){
    this.map.animateToCoordinate({latitude, longitude}, 1);
  }

  closeInputs(){
    this.destinationInput.triggerBlur();
    this.originInput.triggerBlur();
  }

  render() {
    const {history, context} = this.props;
    const {region, busy} = this.state;
    const {delivery} = context.state;
    const {origin, destination} = delivery;

    const showDirections = origin.place_id !== undefined && destination.place_id !== undefined;
    return busy ? <LoadingScreen text="Loading Map"/> : <Container style={{flex: 1}}>

      <MapView ref={c => {this.map = c;}} style={styles.map} showsUserLocation={true} showsMyLocationButton={true} initialRegion={region} onPress={this.closeInputs}>
        {showDirections ? <MapViewDirections origin={origin.location} destination={destination.location} apikey={API_KEY} strokeWidth={3} onReady={(result) => {
          this.map.fitToCoordinates(result.coordinates,  {
            edgePadding: {
              right: Math.round(width / 20) + 100,
              bottom: Math.round(height / 20),
              left: Math.round(width / 20) + 100,
              top: Math.round(height / 20) + 300,
            }});
        }}/> : null}

        {origin.place_id ? <MapView.Marker coordinate={origin.location}>
          <AddressMarker address={origin.name} />
        </MapView.Marker> : null}

        {destination.place_id ? <MapView.Marker coordinate={destination.location}>
          <AddressMarker address={destination.name} />
        </MapView.Marker> : null}
      </MapView>

      <GooglePlacesInput ref={c => {this.destinationInput = c;}} apiKey={API_KEY} onChangeText={(text) => this.onChangeText('destination', text)} onSelect={details => this.onLocationSelect('destination', details)} style={styles.destinationInput} placeholder='Drop-off Location'/>
      <GooglePlacesInput ref={c => {this.originInput = c;}} apiKey={API_KEY} onChangeText={(text) => this.onChangeText('origin', text)}  onSelect={details => this.onLocationSelect('origin', details)} style={styles.originInput} placeholder='Pick-up Location'/>
      {origin.place_id && destination.place_id ? <Button onPress={() => history.push('/Customer/Checkout/Payment')} style={styles.doneButton}><Text>Done</Text></Button> : null}
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
    flex: 1,
    height: 200,
    margin: 2,
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0
  },
  destinationInput: {
    flex: 1,
    height: 200,
    margin: 2,
    position: 'absolute',
    top: 50,
    left: 0,
    right: 0
  }
};

const mapStateToProps = (state, initialProps) => ({
  orderId: getDaoCommandResult(state, 'purchaseCartItems', 'cartItemsDao'),
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(DeliveryMap));


