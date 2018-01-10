import React, {Component} from 'react';
import {Dimensions} from 'react-native';
import Products from 'common/constants/Products';
import {connect} from 'react-redux';
import {Container, Button, Text} from 'native-base';
import {merge, assign} from 'lodash';
import MapView from 'react-native-maps';
import Logger from 'common/Logger';
import LoadingScreen from 'common/components/LoadingScreen';
import PlacesInput from 'common/components/maps/PlacesInput';
import PlacesLookupControl from 'common/components/maps/PlacesLookupControl';
import AddressMarker from 'common/components/maps/AddressMarker';
import MapViewDirections from '../../common/components/maps/MapViewDirections';
import { withRouter } from 'react-router';
import MapService from 'common/services/MapService';
import {getDaoState, isAnyOperationPending} from 'common/dao';

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
  }


  render() {
    const {history, context, client, busy, position} = this.props;
    const {delivery, orderItem} = context.state;
    const {origin, destination} = delivery;

    const region = {
      latitude: position.latitude,
      longitude: position.longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

    const showDirections = origin.googlePlaceId !== undefined && destination.googlePlaceId !== undefined;
    const showDoneButton = origin.googlePlaceId !== undefined && (orderItem.productId == Products.DISPOSAL || destination.googlePlaceId);
    const showDestinationInput = orderItem.productId == Products.DELIVERY;

    const onLocationSelect = (type, details) => {
      const {delivery} = this.props.context.state;
      const newLocation = MapService.parseGooglePlacesData(details);
      Logger.info(`Setting location to ${JSON.stringify(newLocation)}`);

      updateMapRegion(newLocation.latitude, newLocation.longitude);
      context.setState({delivery: merge({}, delivery, {[type]: newLocation})});
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
        {showDirections ? <MapViewDirections client={client} origin={{latitude: origin.latitude, longitude: origin.longitude}} destination={{latitude: destination.latitude, longitude: destination.longitude}} strokeWidth={3} onReady={(result) => {
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

      {showDestinationInput ?  <PlacesLookupControl client={client} addressLabel="Drop-off Location" OnAddressSeleted={details => onLocationSelect('destination', details)}/> : null}
      <PlacesInput client={client} ref={c => {this.originInput = c;}} onChangeText={(text) => onChangeText('origin', text)}  onSelect={details => onLocationSelect('origin', details)} style={styles.originInput} placeholder='Pick-up Location'/>
      {showDoneButton ? <Button onPress={() => history.push('/Customer/Checkout/DeliveryOptions')} style={styles.doneButton}><Text uppercase={false}>Done</Text></Button> : null}
     
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
  state,
  position: getDaoState(state, ['position'], 'userDao'),
  busy: isAnyOperationPending(state, { userDao: 'getCurrentPosition'}),
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(DeliveryMap));


