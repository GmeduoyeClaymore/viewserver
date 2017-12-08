import React, {Component} from 'react';
import {StyleSheet, View, Image, Dimensions} from 'react-native';
import {connect} from 'react-redux';
import {Text, Container, Content, Header, Title, Body, Button, Icon} from 'native-base';
import { Col, Row, Grid } from 'react-native-easy-grid';
import {merge} from 'lodash';
import {getDaoCommandResult} from 'common/dao';
import MapView from 'react-native-maps';
import Logger from 'common/Logger';
import LoadingScreen from 'common/components/LoadingScreen';
import GooglePlacesAutocomplete from 'common/components/GooglePlacesAutocomplete';
import MapViewDirections from 'react-native-maps-directions';
import { withRouter } from 'react-router';
import AddressMarker from './AddressMarker';

const styles = StyleSheet.create({
  map: {
    flex: 1
  },
  originInput: {
    flex: 1,
    height: 200,
    zIndex: 101,
    margin: 2,
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0
  },
  destinationInput: {
    flex: 1,
    height: 200,
    zIndex: 100,
    margin: 2,
    position: 'absolute',
    top: 50,
    left: 0,
    right: 0
  }
});

const API_KEY = 'AIzaSyBAW_qDo2aiu-AGQ_Ka0ZQXsDvF7lr9p3M';
const { width, height } = Dimensions.get('window');
const ASPECT_RATIO = width / height;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;


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

  async componentWillMount(){
    navigator.geolocation.getCurrentPosition(this.onGetCurrentPositionSuccess, undefined, {enableHighAccuracy: true});
  }

  onGetCurrentPositionSuccess(position){
    const {latitude, longitude} = position.coords;
    Logger.warning(`Got users position at ${latitude}, ${longitude}`);
    this.setState({busy: false, region: merge(this.state.region, {latitude, longitude})});
  }

  onLocationSelect(type, details){
    const {name, place_id, geometry} = details;
    const {location} = geometry;
    const {delivery} = this.props.context.state;

    this.props.context.setState({delivery: merge({}, delivery, {[type]: {name, place_id, location: {latitude: location.lat, longitude: location.lng}}})});
    this.updateMapRegion(location.lat, location.lng);
  }

  updateMapRegion(latitude, longitude){
    this.map.animateToCoordinate({latitude, longitude}, 1);
  }

  render() {
    const {region, busy} = this.state;
    const {delivery} = this.props.context.state;
    const {origin, destination} = delivery;

    const showDirections = origin.place_id !== undefined && destination.place_id !== undefined;

    return busy ? <LoadingScreen text="Loading Map"/> : <Container style={{flex: 1}}>


      <MapView ref={c => {this.map = c;}} style={styles.map} showsUserLocation={true} showsMyLocationButton={true} initialRegion={region}>
        {showDirections ? <MapViewDirections origin={origin.location} destination={destination.location} apikey={API_KEY} strokeWidth={3} onReady={(result) => {
          console.log('DONE');
          console.log(width);
          this.map.fitToCoordinates(result.coordinates,  {
            edgePadding: {
              right: Math.round(width / 20) + 100,
              bottom: Math.round(height / 20),
              left: Math.round(width / 20) + 100,
              top: Math.round(height / 20) + 300,
            }});
        }}   onError={(errorMessage) => {
           console.log(errorMessage);
        }}/> : null}

        {origin.place_id ? <MapView.Marker coordinate={origin.location}>
          <AddressMarker address={origin.name} />
        </MapView.Marker> : null}

        {destination.place_id ? <MapView.Marker coordinate={destination.location}>
          <AddressMarker address={destination.name} />
        </MapView.Marker> : null}
      </MapView>

      <GooglePlacesInput onSelect={details => this.onLocationSelect('destination', details)} style={styles.destinationInput} placeholder='Drop-off Location'/>
      <GooglePlacesInput onSelect={details => this.onLocationSelect('origin', details)} style={styles.originInput} placeholder='Pickup Location'/>
    </Container>;
  }
}


DeliveryMap.PropTypes = {
};

const mapStateToProps = (state, initialProps) => ({
  orderId: getDaoCommandResult(state, 'purchaseCartItems', 'cartItemsDao'),
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(DeliveryMap));


const GooglePlacesInput = ({onSelect, style, ...props}) => {
  return (
    <GooglePlacesAutocomplete
      {...props}
      minLength={2} // minimum length of text to search
      autoFocus={false}
      returnKeyType={'search'} // Can be left out for default return key https://facebook.github.io/react-native/docs/textinput.html#returnkeytype
      listViewDisplayed='auto'    // true/false/undefined
      fetchDetails={true}
      renderDescription={row => ResultDescription(row)}
      renderRow={row => ResultRow(row)} // custom description render
      onPress={(data, details = null) => { // 'details' is provided when fetchDetails = true
        onSelect(details);
      }}

      query={{
        // available options: https://developers.google.com/places/web-service/autocomplete
        key: API_KEY,
        language: 'en', // language of the results
        /*    types: '(address)' */// default: 'geocode'
      }}

      styles={{
        container: style,
        textInputContainer: {
          width: '100%',
          backgroundColor: 'transparent'
        },
        textInput: {
          borderWidth: 1,
          borderRadius: 0,
          marginLeft: 0,
          marginRight: 0,
          marginTop: 0,
          marginBottom: 0,
          height: 44
        },
        listView: {
          backgroundColor: '#ffffff',
          borderWidth: 1
        }
      }}

      currentLocation={false} // Will add a 'Current location' button at the top of the predefined places list
      nearbyPlacesAPI='GooglePlacesSearch' // Which API to use: GoogleReverseGeocoding or GooglePlacesSearch
      GooglePlacesSearchQuery={{
        // available options for GooglePlacesSearch API : https://developers.google.com/places/web-service/search
        rankby: 'distance'
      }}

      debounce={200} // debounce the requests in ms. Set to 0 to remove debounce. By default 0ms.
    />
  );
};

const ResultDescription = (row) => {
  const {main_text} = row.structured_formatting;
  return main_text;
};

const ResultRow = (row) => {
  const styles = StyleSheet.create({
    mainText: {
      fontSize: 12
    },
    subText: {
      fontSize: 8
    }
  });

  const {main_text, secondary_text} = row.structured_formatting;

  return (<View><Text style={styles.mainText}>{main_text}</Text>
    <Text style={styles.subText}>{secondary_text}</Text></View>);
};

