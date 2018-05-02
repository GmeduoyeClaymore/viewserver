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

  isMapReady(){
    const {map} = this;
    return map && map.state && map.state.isReady;
  }

  fitMap(newProps){
    try {
      if (!this.isMapReady()){
        Logger.info('Abandoning fit map as map is not ready ' + error);
        return;
      }
      const {map} = this;
      const {order, partnerResponses} = newProps;
      const positions = [];
      positions.push(order.origin);
      positions.push(order.destination);
      partnerResponses.forEach(
        res =>{
          if (res.latitude != undefined && res.longitude != undefined){
            positions.push(res);
          }
        }
      );
      Logger.info(`Calling fit map for ${positions.length} users`);
      if (positions.length){
        map.fitToCoordinates(positions, {
          edgePadding: { top: 50, right: 100, bottom: 250, left: 100 },
          animated: false,
        });
      }
    } catch (error){
      Logger.info('Encountered error in fit map ' + error);
    }
  }

  render(){
    const {order, partnerResponses = [], client} = this.props;
    const {origin, destination} = order;

    return <MapView ref={c => { this.map = c; }} style={{ flex: 1 }} onMapReady={this.fitMap}
      showsUserLocation={false} showsBuidlings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={false}>
      {destination ? <MapViewDirections client={client} locations={[origin, destination]} strokeWidth={3} /> : null}
      {partnerResponses.map( (res, i) => <MapView.Marker key={i} image={locationImg} coordinate={{...res}}/>)}
      <MapView.Marker coordinate={{...origin}}><AddressMarker address={origin.line1}/></MapView.Marker>
      {destination ? <MapView.Marker coordinate={{...destination}}><AddressMarker address={destination.line1}/></MapView.Marker> : null}
    </MapView>;
  }
}
