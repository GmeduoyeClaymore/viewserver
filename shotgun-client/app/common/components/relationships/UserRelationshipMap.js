import React, {Component}  from 'react';
import MapView from 'react-native-maps';
import UserMarker from 'common/components/maps/UserMarker';
import shotgun from 'native-base-theme/variables/shotgun';
import {isEqual, debounce} from 'lodash';
import Logger from 'common/Logger';
import {LoadingScreen} from 'common/components';
import {connect} from 'custom-redux';
import { getDaoState} from 'common/dao';
const ASPECT_RATIO = shotgun.deviceWidth / shotgun.deviceHeight;
const LATITUDE_DELTA = 0.00322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

class UserRelationshipMap extends Component{
  componentDidMount(){
    if (this.map){
      this.fitMap(this.props);
    }
  }

  componentDidUpdate(oldProps){
    const {relatedUsers} = oldProps;
    const {getLocations} = this;

    if (!isEqual(getLocations(oldProps), getLocations(this.props))){
      if (this.mvd){
        this.mvd.fetchAndRenderRoute();
      }
    } else {
      if (!isEqual(relatedUsers, this.props.relatedUsers)){
        this.fitMap(this.props);
      }
    }
  }

  isMapReady = () => {
    return this.map && this.map.state && this.map.state.isReady;
  }

  fitMap = debounce((newProps) => {
    try {
      if (!this.isMapReady()){
        Logger.info('Abandoning fit map as map is not ready ');
        return;
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
      Logger.info(`Calling fit map for ${filteredUsers.length} users`);
      if (filteredUsers.length){
        map.fitToCoordinates(filteredUsers, {
          edgePadding: { top: 150, right: 100, bottom: 200, left: 100 },
          animated: false,
        });
      }
    } catch (error){
      Logger.info('Encountered error in fit map ' + error);
    }
  }, 200);

  getLocations = (props) => {
    const {me, selectedUser} = props;
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
    const {relatedUsers = [], setSelectedUser, me, height, width, isTransitioning, order = {}, hideRelationships, geoLocation} = this.props;
    const {origin = {}} = order;
    const {latitude, longitude} = geoLocation || me;

    const initialRegion = {
      latitude,
      longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

    return isTransitioning ? <LoadingScreen text="Screen transitioning...."/> : <MapView ref={c => { this.map = c; }} style={{ flex: 1, height, width}} onMapReady={() => {this.fitMap(this.props);}}
      region={relatedUsers.length ? undefined : initialRegion} showsUserLocation={true} showsBuildings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={false} >
      {origin.line1 ? <MapView.Marker identifier="origin" coordinate={{...origin}} anchor={{ x: 0.5, y: 1 }}><AddressMarker address={origin.line1} /></MapView.Marker> : null}
      { hideRelationships ? null : relatedUsers.map((user, i) =>
        <MapView.Marker key={user.userId + '-' + i} onPress={() => setSelectedUser(user)} identifier={'userWithProduct' + user.userId}  coordinate={{ ...user }} anchor={{ x: 0.5, y: 1 }}>
          <UserMarker user={user} productId={order ? order.productId : undefined} />
        </MapView.Marker>)}
    </MapView>;
  }
}

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps,
    relatedUsers: getDaoState(state, ['users'], 'userRelationshipDao') || []
  };
};

export default connect(mapStateToProps)(UserRelationshipMap);
