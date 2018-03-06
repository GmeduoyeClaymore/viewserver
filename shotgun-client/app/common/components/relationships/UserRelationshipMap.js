import React, {Component}  from 'react';
import MapView from 'react-native-maps';
import UserMarker from 'common/components/maps/UserMarker';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import shotgun from 'native-base-theme/variables/shotgun';

const ASPECT_RATIO = shotgun.deviceWidth / shotgun.deviceHeight;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

export default class UserRelationshipMap extends Component{
  constructor(props){
    super(props);
    this.fitMap = this.fitMap.bind(this);
  }
  
  fitMap(){
    const {map, props} = this;
    let {me, relatedUsers} = props;
    if (me){
      relatedUsers = [...relatedUsers, me];
    }
    map.fitToCoordinates(relatedUsers.map(c => { return {latitude: c.latitude, longitude: c.longitude};}), {
      edgePadding: { top: 50, right: 100, bottom: 50, left: 100 },
      animated: false,
    });
  }
  
  render(){
    const {me, selectedUser, relatedUsers, context} = this.props;
    const {fitMap} = this;
    const {setSelectedUser} = context;
    const {latitude, longitude} = me;
    
    const initialRegion = {
      latitude,
      longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };
  
    return <MapView ref={c => { this.map = c; }} style={{ flex: 1 }} onMapReady={fitMap} initialRegion={initialRegion}
      showsUserLocation={true} showsBuidlings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={true} >
      {selectedUser ? <MapViewDirections client={client} locations={[me, selectedUser]} strokeWidth={3} /> : null}
      {relatedUsers.map( (user,i) => <MapView.Marker key={user.userId + '' + i} onPress={() => setSelectedUser(user)} key={user.userId} identifier={'userWithProduct' + user.userId}  coordinate={{ ...user }}><UserMarker user={user} /></MapView.Marker>)}
    </MapView>;
  }
}
