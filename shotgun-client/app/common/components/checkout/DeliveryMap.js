import React, {Component}  from 'react';
import { connect, setStateIfIsMounted } from 'custom-redux';
import { Container, Button, Text, Grid, Col, Row} from 'native-base';
import MapView from 'react-native-maps';
import {ErrorRegion, Icon, LoadingScreen} from 'common/components';
import AddressMarker from 'common/components/maps/AddressMarker';
import ProductMarker from 'common/components/maps/ProductMarker';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import { getDaoState, updateSubscriptionAction } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import {TextInput} from 'react-native';
import {isEqual} from 'lodash';
import {addressToText} from 'common/utils';
import {withExternalState} from 'custom-redux';

const ASPECT_RATIO = shotgun.deviceWidth / shotgun.deviceHeight;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

class DeliveryMap extends Component{
  stateKey = 'checkout';
  constructor(props){
    super(props);
    this.doAddressLookup = this.doAddressLookup.bind(this);
    this.setDurationAndDistance = this.setDurationAndDistance.bind(this);
    this.getLocationText = this.getLocationText.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.fitMap = this.fitMap.bind(this);
  }

  componentDidMount(){
    const {isInBackground} = this.props;
    if (isInBackground){
      return;
    }
    this.subscribeToUsersForProduct(this.getOptionsFromProps(this.props));
  }

  async subscribeToUsersForProduct(options){
    const {dispatch} = this.props;
    dispatch(updateSubscriptionAction('userRelationshipDao', options));
    this.setState({oldOptions: options});
  }

  getOptionsFromProps(props){
    const {selectedProduct, position} = props;
    return {
      selectedProduct,
      position,
      columnsToSort: [{name: 'distance', direction: 'asc'}]
    };
  }
  componentWillReceiveProps(newProps){
    const {oldOptions, isInBackground} = newProps;
    if (isInBackground){
      return;
    }
    const newOptions = this.getOptionsFromProps(newProps);
    if (!isEqual(newOptions, oldOptions)){
      this.subscribeToUsersForProduct(newOptions);
    }
    const {destination, origin} = this.props;
    if (destination != newProps.destination || origin != newProps.origin){
      this.fitMap();
    }
  }

  async onChangeText(location, field, value){
    const {delivery} = this.props;
    newLocation = {...delivery[location], [field]: value};
    this.setState({delivery: {...delivery, [location]: newLocation}});
  }

  getLocationText(address, addressKey, placeholder){
    style = address.line1 ? {} : styles.locationTextPlaceholder;
    text = addressToText(address) || placeholder;
    const {onChangeText} = this;
    return  <Row  style={styles.inputRow} onPress={() => this.doAddressLookup(placeholder, addressKey)}>
      <Icon name="pin" paddedIcon originPin /><Row>
        {address.line1 !== undefined ? <Col size={30}>
          <TextInput placeholder='flat/business'  multiline={false} style={{paddingTop: 0, textAlignVertical: 'top'}} underlineColorAndroid='transparent' placeholderTextColor={shotgun.silver} value={address.flatNumber}  onChangeText={(value) => onChangeText(addressKey, 'flatNumber', value)} validationSchema={validationSchema.flatNumber} maxLength={10}/>
        </Col> : null}
        <Col size={70}>
          <Text style={style} >{text}</Text>
        </Col>
      </Row></Row>;
  }

  setDurationAndDistance({distance, duration}){
    const {delivery} = this.props;
    this.setState({delivery: {...delivery, distance: Math.round(distance),  duration: Math.round(duration)}});
    this.fitMap();
  }

  doAddressLookup(addressLabel, addressKey){
    const {history, parentPath} = this.props;
    history.push(`${parentPath}/AddressLookup`, {addressLabel, addressPath: ['delivery', addressKey]});
  }

  fitMap(){
    const {map} = this;
    const {destination, origin} = this.props;
    if ((origin.line1 !== undefined && destination.line1 !== undefined) && map) {
      map.fitToCoordinates([{latitude: origin.latitude, longitude: origin.longitude}, {latitude: destination.latitude, longitude: destination.longitude}], {
        edgePadding: { top: 50, right: 100, bottom: 50, left: 100 },
        animated: false,
      });
    }
  }

  render(){
    const {fitMap, setDurationAndDistance, getLocationText} = this;
    const {destination, origin, isTransitioning, showDirections, supportsDestination, supportsOrigin, disableDoneButton, client, me, next, errors, selectedProduct, usersWithProduct, history} = this.props;
    const {latitude, longitude} = me;
  
    const initialRegion = {
      latitude,
      longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

    return <Container style={{ flex: 1 }}>
      <Grid>
        <Row size={85}>
          <ErrorRegion errors={errors}>
            {isTransitioning ? <LoadingScreen text="Screen transitioning...."/> : <MapView ref={c => { this.map = c; }} style={{ flex: 1 }} onMapReady={fitMap} initialRegion={initialRegion}
              showsUserLocation={true} showsBuidlings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={true} >
              {showDirections ? <MapViewDirections client={client} locations={[origin, destination]} onReady={setDurationAndDistance} strokeWidth={3} /> : null}
              {origin.line1 ? <MapView.Marker identifier="origin" coordinate={{...origin}}><AddressMarker address={origin.line1} /></MapView.Marker> : null}
              {destination.line1 ? <MapView.Marker identifier="destination" coordinate={{ ...destination }}><AddressMarker address={destination.line1} /></MapView.Marker> : null}
              {usersWithProduct.map( user => <MapView.Marker key={user.userId} identifier={'userWithProduct' + user.userId}  coordinate={{ ...user }}><ProductMarker product={selectedProduct} /></MapView.Marker>)}
            </MapView>}
          </ErrorRegion>
          <Button transparent style={styles.backButton} onPress={() => history.goBack()} >
            <Icon name='back-arrow'/>
          </Button>
        </Row>
        {supportsOrigin ? getLocationText(origin, 'origin', 'Enter pick-up location') : null}
        {supportsDestination ? getLocationText(destination, 'destination', 'Enter drop-off location') : null}
      </Grid>
      <Button fullWidth paddedBottom iconRight onPress={() => history.push(next)} disabled={disableDoneButton}>
        <Text uppercase={false}>Continue</Text>
        <Icon name='forward-arrow' next/>
      </Button>
    </Container>;
  }
}

const validationSchema = {
  flatNumber: yup.string().max(30)
};

const styles = {
  backButton: {
    position: 'absolute',
    left: 0,
    top: 0
  },
  locationTextPlaceholder: {
    color: shotgun.silver
  },
  inputRow: {
    padding: shotgun.contentPadding
  }
};

const mapStateToProps = (state, initialProps) => {
  const {delivery, selectedContentType, selectedProduct, isInBackground} = initialProps;
  const {destination, origin, distance, duration} = delivery;
  const showDirections = origin.line1 !== undefined && destination.line1 !== undefined;
  const supportsDestination = selectedContentType.destination;
  const supportsOrigin = selectedContentType.origin;
  const disableDoneButton = origin.line1 == undefined || !distance || !duration || (supportsDestination && destination.line1 == undefined) || (!supportsDestination && !supportsOrigin) || (origin.latitude && origin.longitude && origin.longitude == destination.longitude);

  return {
    ...initialProps,
    state,
    me: getDaoState(state, ['user'], 'userDao'),
    delivery, selectedProduct, selectedContentType, destination, origin, showDirections, supportsDestination, supportsOrigin, disableDoneButton,
    usersWithProduct: getDaoState(state, ['users'], 'userRelationshipDao') || []
  };
};

export default withExternalState(mapStateToProps)(DeliveryMap);


