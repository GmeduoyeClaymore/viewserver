import React, {Component}  from 'react';
import { connect, setStateIfIsMounted } from 'custom-redux';
import { Container, Button, Text, Grid, Col, Row} from 'native-base';
import MapView from 'react-native-maps';
import {LoadingScreen, ErrorRegion, Icon} from 'common/components';
import AddressMarker from 'common/components/maps/AddressMarker';
import ProductMarker from 'common/components/maps/ProductMarker';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import { withRouter } from 'react-router';
import { getDaoState, getOperationError, isOperationPending, isAnyOperationPending, updateSubscriptionAction } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import {TextInput} from 'react-native';
import {isEqual} from 'lodash';

const ASPECT_RATIO = shotgun.deviceWidth / shotgun.deviceHeight;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

class DeliveryMap extends Component{
  constructor(props){
    super(props);
    this.doAddressLookup = this.doAddressLookup.bind(this);
    this.setLocation = this.setLocation.bind(this);
    this.setDurationAndDistance = this.setDurationAndDistance.bind(this);
    this.getLocationText = this.getLocationText.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.fitMap = this.fitMap.bind(this);
    this.state = {};
    setStateIfIsMounted(this);
  }

  componentDidMount(){
    this.subscribeToUsersForProduct(this.getOptionsFromProps(this.props));
  }

  async subscribeToUsersForProduct(options){
    const {dispatch} = this.props;
    dispatch(updateSubscriptionAction('userProductDao', options));
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
    const {oldOptions} = this.state;
    const newOptions = this.getOptionsFromProps(newProps);
    if (!isEqual(newOptions, oldOptions)){
      this.subscribeToUsersForProduct(newOptions);
    }
  }

  async onChangeText(location, field, value){
    const {delivery, context} = this.props;
    newLocation = {...delivery[location], [field]: value};
    context.setState({delivery: {...delivery, [location]: newLocation}});
  }

  getLocationText(address, addressKey, placeholder){
    style = address.line1 ? {} : styles.locationTextPlaceholder;
    text = address.line1 ? `${address.line1}, ${address.postCode}` : placeholder;
    const {onChangeText, setLocation, doAddressLookup} = this;
    return  <Row>
      {address.line1 !== undefined ? <Col size={30}>
        <TextInput placeholder='flat/business'  multiline={false} style={{paddingTop: 0, textAlignVertical: 'top'}} underlineColorAndroid='transparent' placeholderTextColor={shotgun.silver} value={address.flatNumber}  onChangeText={(value) => onChangeText(addressKey, 'flatNumber', value)} validationSchema={validationSchema.flatNumber} maxLength={10}/>
      </Col> : null}
      <Col size={70}>
        <Text style={style} onPress={() => doAddressLookup(placeholder, a => setLocation(a, addressKey))}>{text}</Text>
      </Col>
    </Row>;
  }

  setLocation(address, addressKey){
    const {delivery, history, context} = this.props;
    context.setState({delivery: {...delivery, [addressKey]: address }}, () => history.push('/Customer/Checkout/DeliveryMap'));
  }

  setDurationAndDistance({distance, duration}){
    const {delivery, context} = this.props;
    context.setState({delivery: {...delivery, distance: Math.round(distance),  duration: Math.round(duration)}});
  }

  doAddressLookup(addressLabel, onAddressSelected){
    const {history} = this.props;
    history.push('/Customer/Checkout/AddressLookup', {addressLabel, onAddressSelected});
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

    const {busy, destination, origin, showDirections, supportsDestination, supportsOrigin, disableDoneButton, client, position, navigationStrategy, errors, selectedProduct, usersWithProduct} = this.props;

    if (busy){
      return <LoadingScreen text="Waiting for position from device" />;
    }
    if (errors){
      return <ErrorRegion errors={errors}/>;
    }
    const {latitude, longitude} = position;
  
    const initialRegion = {
      latitude,
      longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

    return <Container style={{ flex: 1 }}>
      <Grid>
        <Row size={85}>
          <MapView ref={c => { this.map = c; }} style={{ flex: 1 }} onMapReady={fitMap} initialRegion={initialRegion}
            showsUserLocation={true} showsBuidlings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={true} >
            {showDirections ? <MapViewDirections client={client} locations={[origin, destination]} onReady={setDurationAndDistance} strokeWidth={3} /> : null}
            {origin.line1 ? <MapView.Marker identifier="origin" coordinate={{...origin}}><AddressMarker address={origin.line1} /></MapView.Marker> : null}
            {destination.line1 ? <MapView.Marker identifier="destination" coordinate={{ ...destination }}><AddressMarker address={destination.line1} /></MapView.Marker> : null}
            {usersWithProduct.map( user => <MapView.Marker key={user.userId} identifier={'userWithProduct' + user.userId}  coordinate={{ ...user }}><ProductMarker product={selectedProduct} /></MapView.Marker>)}
          </MapView>
          <Button transparent style={styles.backButton}>
            <Icon name='back-arrow' onPress={() => navigationStrategy.prev()} />
          </Button>
        </Row>
        <Row size={15} style={styles.inputRow}>
          <Col>
            {supportsOrigin ? <Row>
              <Icon name="pin" paddedIcon originPin />
              {getLocationText(origin, 'origin', 'Enter pick-up location')}
            </Row> : null}
            {supportsDestination ? <Row>
              <Icon name="pin" paddedIcon />
              {getLocationText(destination, 'destination', 'Enter drop-off location')}
            </Row> : null}
          </Col>
        </Row>
      </Grid>
      <Button fullWidth paddedBottom iconRight onPress={() => navigationStrategy.next()} disabled={disableDoneButton}>
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
  const {context} = initialProps;
  const {delivery, selectedContentType, selectedProduct} = context.state;
  const {destination, origin} = delivery;
  const showDirections = origin.line1 !== undefined && destination.line1 !== undefined;
  const supportsDestination = selectedContentType.destination;
  const supportsOrigin = selectedContentType.origin;
  const disableDoneButton = origin.line1 == undefined || (supportsDestination && destination.line1 == undefined) || (!supportsDestination && !supportsOrigin) || (origin.latitude && origin.longitude && origin.longitude == destination.longitude);
  let positionFromDelivery;
  if (origin.line1 !== undefined) {
    positionFromDelivery = origin;
  } else if (destination.line1 !== undefined) {
    positionFromDelivery = destination;
  }
  return {
    ...initialProps,
    state,
    delivery, selectedProduct, selectedContentType, destination, origin, showDirections, supportsDestination, supportsOrigin, disableDoneButton,
    position: getDaoState(state, ['position'], 'userDao'),
    usersWithProduct: getDaoState(state, ['users', 'productUsers'], 'userProductDao') || [],
    busy: isAnyOperationPending(state, [{userDao: 'getCurrentPosition'}, {userProductDao: 'updateSubscription'}]),
    errors: getOperationError(state, 'userDao', 'getCurrentPosition') };
};

export default withRouter(connect(
  mapStateToProps
)(DeliveryMap));


