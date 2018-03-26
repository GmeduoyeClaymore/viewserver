import React, {Component}  from 'react';
import MapView from 'react-native-maps';
import UserMarker from 'common/components/maps/UserMarker';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import shotgun from 'native-base-theme/variables/shotgun';
import {isEqual} from 'lodash';
const ASPECT_RATIO = shotgun.deviceWidth / shotgun.deviceHeight;
const LATITUDE_DELTA = 0.00322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

export default class UserRelationshipMap extends Component{
  constructor(props){
    super(props);
    this.fitMap = this.fitMap.bind(this);
    this.getLocations = this.getLocations.bind(this);
  }
  
  fitMap(newProps, flagMapReady){
    if (!flagMapReady && !this.isMapReady){
      return;
    }
    if (flagMapReady){
      this.isMapReady = flagMapReady;
    }
    const {map} = this;
    let {me} = newProps;

    const {location} = newProps;
    me = location || me;
    let {relatedUsers} = newProps;
    if (me){
      relatedUsers = [...relatedUsers, me];
    }
    const filteredUsers = relatedUsers.filter(c=> c.latitude != undefined && c.longitude != undefined ).map(c => {return {latitude: c.latitude, longitude: c.longitude};});
    if (filteredUsers.length){
      map.fitToCoordinates(filteredUsers, {
        edgePadding: { top: 50, right: 100, bottom: 50, left: 100 },
        animated: false,
      });
    }
  }

  componentDidMount(){
    if (this.map){
      this.fitMap(this.props);
    }
  }

  componentWillReceiveProps(newProps){
    const {relatedUsers} = newProps;
    if (!isEqual(relatedUsers, this.props.relatedUsers)){
      this.fitMap(newProps);
    }
  }

  getLocations(){
    const {me, selectedUser} = this.props;
    const result = [];
    if (me && me.latitude && me.longitude){
      const {latitude, longitude} = me;
      result.push({latitude, longitude});
    }
    if (selectedUser && selectedUser.latitude && selectedUser.longitude){
      const {latitude, longitude} = selectedUser;
      result.push({latitude, longitude});
    }
    return result;
  }

  render(){
    const {selectedUser, relatedUsers = [], context, client, geoLocation} = this.props;
    let {me} = this.props;
    me = geoLocation && geoLocation.latitude ? geoLocation : me;
    const {fitMap, getLocations} = this;
    const {setSelectedUser} = context;
    const {latitude, longitude} = me;
    
    const initialRegion = {
      latitude,
      longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

  
    return <MapView ref={c => { this.map = c; }} style={{ flex: 1 }} onMapReady={() => {fitMap(this.props, true);}} region={relatedUsers.length ? undefined : initialRegion} showsUserLocation={true} showsBuidlings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={true} >
      {selectedUser && me ? <MapViewDirections client={client} locations={getLocations()} strokeWidth={3} /> : null}
      {relatedUsers.map( (user, i) => <MapView.Marker key={user.userId + '' + i} onPress={() => setSelectedUser(user)} identifier={'userWithProduct' + user.userId}  coordinate={{ ...user }}><UserMarker user={user} /></MapView.Marker>)}
    </MapView>;
  }
}
