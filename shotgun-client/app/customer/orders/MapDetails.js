import MapViewDirections from 'common/components/maps/MapViewDirections';
import locationImg from 'common/assets/location.png';
import MapView from 'react-native-maps';
import React, {Component} from 'react';

export default class MapDetails extends Component{
  constructor(props){
    super(props);
  }
  render(){
    const {origin, destination, driverPosition} = this.props;
    <MapView ref={c => { map = c; }} style={{ flex: 1 }} onMapReady={fitMap} initialRegion={region}
      showsUserLocation={false} showsBuidlings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={false}>
      {contentType.destination ? <MapViewDirections client={client} locations={[origin, destination]} strokeWidth={3} /> : null}
      <MapView.Marker image={locationImg} coordinate={{...driverPosition}}/>
      <MapView.Marker coordinate={{...origin}}><AddressMarker address={origin.line1}/></MapView.Marker>
      {contentType.destination ? <MapView.Marker coordinate={{...destination}}><AddressMarker address={destination.line1}/></MapView.Marker> : null}
    </MapView>;
  }
}
