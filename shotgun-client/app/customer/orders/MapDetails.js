import MapViewDirections from 'common/components/maps/MapViewDirections';
import locationImg from 'common/assets/location.png';
import MapView from 'react-native-maps';
import React, {Component} from 'react';
import {Dimensions} from 'react-native';
const {width, height} = Dimensions.get('window');


const ASPECT_RATIO = width / height;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;


export default class MapDetails extends Component{
  constructor(props){
    super(props);
  }
  render(){
    let map;
    const {origin, destination, driverPosition, contentType} = this.props;
    const fitMap = () => {
      if ((origin.line1 !== undefined && destination.line1 !== undefined)) {
        map.fitToElements(false);
      }
    };

    const region = {
      latitude: driverPosition.latitude,
      longitude: driverPosition.longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

    return <MapView ref={c => { map = c; }} style={{ flex: 1 }} onMapReady={fitMap} initialRegion={region}
      showsUserLocation={false} showsBuidlings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={false}>
      {contentType.destination ? <MapViewDirections client={client} locations={[origin, destination]} strokeWidth={3} /> : null}
      <MapView.Marker image={locationImg} coordinate={{...driverPosition}}/>
      <MapView.Marker coordinate={{...origin}}><AddressMarker address={origin.line1}/></MapView.Marker>
      {contentType.destination ? <MapView.Marker coordinate={{...destination}}><AddressMarker address={destination.line1}/></MapView.Marker> : null}
    </MapView>;
  }
}
