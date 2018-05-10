import React, {Component} from 'react';
import {View} from 'native-base';
import { OriginDestinationSummary} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';
import MapView from 'react-native-maps';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import AddressMarker from 'common/components/maps/AddressMarker';
import ProductMarker from 'common/components/maps/ProductMarker';
import Logger from 'common/Logger';
const ASPECT_RATIO = shotgun.deviceWidth / shotgun.deviceHeight;
const LATITUDE_DELTA = 0.00322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;
class CustomerJourneyOrderInProgress extends Component {
  constructor(props){
    super(props);
  }
  isMapReady(){
    const {map} = this;
    return map && map.state && map.state.isReady;
  }
    fitMap = () => {
      try {
        const {order} = this.props;
        const {map} = this;
        const {assignedPartner = {}, origin, destination = {}} = order;
        const coordinates = [assignedPartner, origin, destination];
        map.fitToCoordinates(coordinates, {
          edgePadding: { top: 50, right: 100, bottom: 50, left: 100 },
          animated: false,
        });
      } catch (error){
        Logger.error('Encountered error in fit map ' + error);
      }
    }
    render() {
      const {order = {}, client,  height, width} = this.props;
      const {assignedPartner = {}, origin, destination = {}} = order;
        
      const {latitude, longitude} = assignedPartner;
      const initialPosition = {latitude, longitude};

      const region = {
        latitude,
        longitude,
        latitudeDelta: LATITUDE_DELTA,
        longitudeDelta: LONGITUDE_DELTA
      };

      return <View>
        <View>
          <MapView style={{ height: height / 3, width: width / 1.1, padding: 10 }} ref={c => { this.map = c; }}  onMapReady={this.fitMap} initialRegion={region}
            showsBuildings={false} showsPointsOfInterest={false} toolbarEnabled={false} >
            <MapViewDirections client={client} locations={[origin, destination]} strokeWidth={3} strokeColor={shotgun.brandSecondary}/>
            {destination ? <MapViewDirections client={client} locations={[origin, destination]} strokeWidth={3} /> : null}
            {origin ? <MapView.Marker coordinate={{...origin}}><AddressMarker address={origin.line1}/></MapView.Marker> : null}
            {destination ? <MapView.Marker coordinate={{...destination}}><AddressMarker address={destination.line1}/></MapView.Marker> : null}
            {destination ? <MapView.Marker coordinate={{...assignedPartner}}><ProductMarker product={order.orderProduct}/></MapView.Marker> : null}
          </MapView>
        </View>
      </View>;
    }
}

export default CustomerJourneyOrderInProgress;
