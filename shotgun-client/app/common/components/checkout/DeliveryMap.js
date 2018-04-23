import React, {Component}  from 'react';
import { withExternalState} from 'custom-redux';
import {Input, Container, Button, Text, Grid, Col, Row, Item} from 'native-base';
import MapView from 'react-native-maps';
import {ErrorRegion, Icon, LoadingScreen} from 'common/components';
import AddressMarker from 'common/components/maps/AddressMarker';
import ProductMarker from 'common/components/maps/ProductMarker';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import {getDaoState, updateSubscriptionAction} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import {isEqual, debounce} from 'lodash';
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

  onChangeText = (location, field, value) => {
    const {delivery} = this.props;
    newLocation = {...delivery[location], [field]: value};
    this.setState({delivery: {...delivery, [location]: newLocation}});
  }

  getLocationText = (address, addressKey, placeholder) => {
    return  <Item style={styles.inputRow} onPress={() => this.doAddressLookup(placeholder, addressKey)}>
      <Icon name="pin" style={styles.inputPin} originPin />
      {address.line1 !== undefined ? <Col size={30} style={{}}>
        <Input placeholder='flat/business' style={styles.flatInput} value={address.flatNumber} placeholderTextColor={shotgun.silver} onChangeText={(value) => this.onChangeText(addressKey, 'flatNumber', value)} validationSchema={validationSchema.flatNumber} maxLength={30}/>
      </Col> : null}
      <Col size={70}>
        <Text style={address.line1 ? {} : styles.locationTextPlaceholder}>{addressToText(address) || placeholder}</Text>
      </Col>
    </Item>;
  }

  setDurationAndDistance = ({distance, duration}) => {
    const {delivery} = this.props;
    this.setState({delivery: {...delivery, distance: Math.round(distance),  duration: Math.round(duration)}});
    this.fitMap();
  }

  doAddressLookup = (addressLabel, addressKey) => {
    const {history, parentPath} = this.props;
    history.push(`${parentPath}/AddressLookup`, {addressLabel, addressPath: ['delivery', addressKey]});
  }

  getOriginAndDestination = () => {
    const {destination, origin} = this.props;
    return {destination, origin};
  }

  componentDidUpdate(oldProps){
    if (!isEqual(this.getOriginAndDestination(oldProps), this.getOriginAndDestination(this.props))){
      if (this.mvd){
        this.mvd.fetchAndRenderRoute();
      }
      this.fitMap();
    }
  }


  fitMap = debounce(() => {
    const {destination, origin} = this.props;
    if ((origin.line1 !== undefined && destination.line1 !== undefined) && this.map) {
      this.map.fitToCoordinates([{latitude: origin.latitude, longitude: origin.longitude}, {latitude: destination.latitude, longitude: destination.longitude}], {
        edgePadding: { top: 250, right: 100, bottom: 150, left: 100 },
        animated: true,
      });
    }
  }, 1000)

  render(){
    const {destination, origin, isTransitioning, showDirections, supportsDestination, supportsOrigin, disableDoneButton, client, me, next, errors, selectedProduct, usersWithProduct, history} = this.props;

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
              {supportsOrigin ? this.getLocationText(origin, 'origin', 'Pick-up location') : null}
              {supportsDestination ? this.getLocationText(destination, 'destination', 'Drop-off location') : null}
            </Col>
          </Row>
          {isTransitioning ? <LoadingScreen text="Screen transitioning...."/> :
            <MapView ref={c => { this.map = c; }} style={{ flex: 1 }} onMapReady={this.fitMap} initialRegion={initialRegion}
              showsUserLocation={true} showsBuildings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={true} >
              {showDirections ? <MapViewDirections ref={ref => {this.mvd = ref;}} client={client} locations={[origin, destination]} onReady={this.setDurationAndDistance} strokeWidth={3} /> : null}
              {origin.line1 ? <MapView.Marker identifier="origin" coordinate={{...origin}}><AddressMarker address={origin.line1} /></MapView.Marker> : null}
              {destination.line1 ? <MapView.Marker identifier="destination" coordinate={{ ...destination }}><AddressMarker address={destination.line1} /></MapView.Marker> : null}
              {usersWithProduct.map( user => <MapView.Marker key={user.userId} identifier={`userWithProduct${user.userId}`}  coordinate={{ ...user }}><ProductMarker product={selectedProduct} /></MapView.Marker>)}
            </MapView>}
        </Row>
      </Grid>
      <ErrorRegion errors={errors}/>
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
  const {delivery, selectedContentType, selectedProduct} = initialProps;
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


