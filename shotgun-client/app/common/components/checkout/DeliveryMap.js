import React, {Component}  from 'react';
import { withExternalState} from 'custom-redux';
import {Input, Container, Button, Text, Grid, Col, Row, Item} from 'native-base';
import MapView from 'react-native-maps';
import {Icon, LoadingScreen} from 'common/components';
import AddressMarker from 'common/components/maps/AddressMarker';
import ProductMarker from 'common/components/maps/ProductMarker';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import {getDaoState, updateSubscriptionAction} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import {isEqual, debounce} from 'lodash';
import * as ContentTypes from 'common/constants/ContentTypes';
import {addressToText} from 'common/components/maps/MapUtils';

class DeliveryMap extends Component{
  stateKey = 'checkout';

  componentDidMount(){
    const {isInBackground} = this.props;
    if (isInBackground){
      return;
    }
    this.subscribeToUsersForProduct(this.getOptionsFromProps(this.props));
  }

  componentDidUpdate(oldProps){
    const {destination, origin} = this.props;
    if (destination != oldProps.destination || origin != oldProps.origin){
      if (this.mvd){
        this.mvd.fetchAndRenderRoute();
      }
      this.fitMap();
    }
  }

  async subscribeToUsersForProduct(options){
    const {dispatch} = this.props;
    dispatch(updateSubscriptionAction('userRelationshipDao', options));
    this.setState({oldOptions: options});
  }

  getOptionsFromProps(props){
    const {order, position} = props;
    return {
      selectedProduct: order.orderProduct,
      position,
      columnsToSort: [{name: 'distance', direction: 'asc'}]
    };
  }

  onChangeText = async(location, field, value) => {
    const {order} = this.props;
    const currentLocation = order[location];
    this.setState({order: {...order, [location]: {...currentLocation, [field]: value}}});
  }

  getLocationText = (address, addressKey, placeholder) => {
    return  <Item style={styles.inputRow} onPress={() => this.doAddressLookup(placeholder, addressKey)}>
      <Icon name="pin" style={styles.inputPin} originPin />
      {address.line1 !== undefined ? <Col size={30}>
        <Input placeholder='flat/business' style={styles.flatInput} value={address.flatNumber} placeholderTextColor={shotgun.silver} onChangeText={(value) => this.onChangeText(addressKey, 'flatNumber', value)} validationSchema={validationSchema.flatNumber} maxLength={30}/>
      </Col> : null}
      <Col size={70}>
        <Text numberOfLines={1} style={address.line1 ? {} : styles.locationTextPlaceholder}>{addressToText(address) || placeholder}</Text>
      </Col>
    </Item>;
  }

  setDurationAndDistance = ({distance, duration}) => {
    const {setStateWithPath, dispatch} = this.props;
    setStateWithPath({distance: Math.round(distance),  duration: Math.round(duration)}, ['order', 'distanceAndDuration'], undefined,  dispatch);
    this.fitMap();
  }

  doAddressLookup = (addressLabel, addressKey) => {
    const {history, parentPath} = this.props;
    history.push(`${parentPath}/AddressLookup`, {addressLabel, addressPath: ['order', addressKey]});
  }

  fitMap = debounce(() => {
    const {destination, origin} = this.props;
    const coordinates = [];

    if (origin.line1 != undefined){
      coordinates.push({latitude: origin.latitude, longitude: origin.longitude});
    }

    if (destination.line1 != undefined){
      coordinates.push({latitude: destination.latitude, longitude: destination.longitude});
    }

    if (this.map && coordinates.length == 2) {
      this.map.fitToCoordinates(coordinates, {
        edgePadding: { top: 450, right: 100, bottom: 150, left: 100 },
        animated: true,
      });
    }
  }, 500)

  render(){
    const {destination, origin, isTransitioning, client, me, next, distanceAndDuration, usersWithProduct, history, order} = this.props;
    const {distance, duration} = distanceAndDuration;
    const showDirections = origin.line1 !== undefined && destination.line1 !== undefined;
    const disableDoneButton = origin.line1 == undefined || !distance || !duration || destination.line1 == undefined || (origin.latitude && origin.longitude && origin.longitude == destination.longitude);

    if (!me){
      return <LoadingScreen text="Waiting for user ..."/>;
    }

    const {latitude, longitude} = me;
  
    const initialRegion = {
      latitude,
      longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

    return <Container>
      <Button transparent style={styles.backButton} onPress={() => history.goBack()} >
        <Icon name='back-arrow'/>
      </Button>
      <Grid>
        <Row>
          <Row style={styles.inputRowHolder}>
            <Col>
              {this.getLocationText(origin, 'origin', 'Pick-up location')}
              {this.getLocationText(destination, 'destination', 'Drop-off location')}
            </Col>
          </Row>
          {isTransitioning ? <LoadingScreen text="Screen transitioning...."/> :
            <MapView ref={c => { this.map = c; }} style={{ flex: 1 }} onMapReady={this.fitMap} initialRegion={initialRegion}
              showsUserLocation={true} showsBuildings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={false} >
              {showDirections ? <MapViewDirections ref={ref => {this.mvd = ref;}} client={client} locations={[origin, destination]} onReady={this.setDurationAndDistance} strokeWidth={3} /> : null}
              {origin.line1 ? <MapView.Marker identifier="origin" coordinate={{...origin}} anchor={{ x: 0.5, y: 1 }}><AddressMarker address={origin.line1} /></MapView.Marker> : null}
              {destination.line1 ? <MapView.Marker identifier="destination" coordinate={{ ...destination }} anchor={{ x: 0.5, y: 1 }}><AddressMarker address={destination.line1} color={shotgun.brandDark} /></MapView.Marker> : null}
              {usersWithProduct.map( user => <MapView.Marker key={user.userId} identifier={`userWithProduct${user.userId}`}  coordinate={{ ...user }}><ProductMarker product={order.orderProduct} /></MapView.Marker>)}
            </MapView>}
        </Row>
      </Grid>
      <Button style={styles.nextButton} iconRight onPress={() => history.push(next)} disabled={disableDoneButton}>
        <Text uppercase={false} style={{alignSelf: 'center'}}>Continue</Text>
        <Icon name='forward-arrow' next/>
      </Button>
    </Container>;
  }
}

const ASPECT_RATIO = shotgun.deviceWidth / shotgun.deviceHeight;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

const validationSchema = {
  flatNumber: yup.string().max(30)
};

const styles = {
  inputRowHolder: {
    position: 'absolute',
    top: 45,
    left: 15,
    right: 15,
    zIndex: 2
  },
  inputRow: {
    marginBottom: 15,
    backgroundColor: shotgun.brandPrimary,
    padding: 10,
  },
  nextButton: {
    position: 'absolute',
    bottom: 15,
    left: 15,
    right: 15,
    zIndex: 2,
    justifyContent: 'center'
  },
  backButton: {
    position: 'absolute',
    left: 0,
    top: 6,
    zIndex: 2
  },
  locationTextPlaceholder: {
    color: shotgun.silver
  },
  flatInput: {
    padding: 0,
    height: 12,
    lineHeight: 18
  },
  inputPin: {
    paddingRight: 15
  }
};

const mapStateToProps = (state, initialProps) => {
  const {selectedContentType, order} = initialProps;
  const {destination = {}, origin = {}, distanceAndDuration = {}} = order;

  return {
    ...initialProps,
    state,
    me: getDaoState(state, ['user'], 'userDao'),
    selectedContentType,
    distanceAndDuration,
    destination,
    origin,
    usersWithProduct: getDaoState(state, ['users'], 'userRelationshipDao') || []
  };
};

export default withExternalState(mapStateToProps)(DeliveryMap);


