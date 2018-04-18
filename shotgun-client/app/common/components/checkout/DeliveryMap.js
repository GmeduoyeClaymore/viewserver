import React, {Component}  from 'react';
import { withExternalState} from 'custom-redux';
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
import {isEqual, debounce} from 'lodash';
import {addressToText} from 'common/utils';
import * as ContentTypes from 'common/constants/ContentTypes';
const ASPECT_RATIO = shotgun.deviceWidth / shotgun.deviceHeight;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary().
  property('supportsDestination', false).
    delivery(true).
  property('supportsOrigin', true);

class DeliveryMap extends Component{
  stateKey = 'checkout';
  constructor(props){
    super(props);
    this.doAddressLookup = this.doAddressLookup.bind(this);
    this.setDurationAndDistance = this.setDurationAndDistance.bind(this);
    this.getLocationText = this.getLocationText.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.fitMap = debounce(this.fitMap.bind(this), 1000);

    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
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
    return  <Row fullWidth style={styles.inputRow} onPress={() => this.doAddressLookup(placeholder, addressKey)}>
      <Icon name="pin" style={{paddingRight: 15}} originPin /><Row>
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


  getOriginAndDestination(props){
    const {destination, origin} = props;
    return {destination, origin};
  }

  componentDidUpdate(oldProps){
    const {getOriginAndDestination} = this;
    if (!isEqual(getOriginAndDestination(oldProps), getOriginAndDestination(this.props))){
      if (this.mvd){
        this.mvd.fetchAndRenderRoute();
      }
      this.fitMap(this.props);
    }
  }


  fitMap(){
    const {map} = this;
    const {destination, origin} = this.props;
    if ((origin.line1 !== undefined && destination.line1 !== undefined) && map) {
      map.fitToCoordinates([{latitude: origin.latitude, longitude: origin.longitude}, {latitude: destination.latitude, longitude: destination.longitude}], {
        edgePadding: { top: 250, right: 100, bottom: 150, left: 100 },
        animated: true,
      });
    }
  }

  render(){
    const {fitMap, setDurationAndDistance, getLocationText, resources} = this;
    const {destination, origin, isTransitioning, showDirections, disableDoneButton, client, me, next, errors, selectedProduct, usersWithProduct, history} = this.props;
    const {supportsOrigin, supportsDestination} = resources;
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

    return <Container style={{ flex: 1 }}>
      <Grid>
        <Row size={85}>
          <Row fullWidth style={{position: 'absolute', top: 40, left: 15, zIndex: 2 }}>
            <Col>
              {supportsOrigin ? getLocationText(origin, 'origin', 'Enter pick-up location') : null}
              {supportsDestination ? getLocationText(destination, 'destination', 'Enter drop-off location') : null}
            </Col>
          </Row>
          <ErrorRegion errors={errors}>
            {isTransitioning ? <LoadingScreen text="Screen transitioning...."/> : <MapView ref={c => { this.map = c; }} style={{ flex: 1 }} onMapReady={fitMap} initialRegion={initialRegion}
              showsUserLocation={true} showsBuidlings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={true} >
              {showDirections && origin && destination ? <MapViewDirections ref={ref => {this.mvd = ref;}} client={client} locations={[origin, destination]} onReady={setDurationAndDistance} strokeWidth={3} /> : null}
              {origin.line1 ? <MapView.Marker identifier="origin" coordinate={{...origin}}><AddressMarker address={origin.line1} /></MapView.Marker> : null}
              {destination.line1 ? <MapView.Marker identifier="destination" coordinate={{ ...destination }}><AddressMarker address={destination.line1} /></MapView.Marker> : null}
              {usersWithProduct.map( user => <MapView.Marker key={user.userId} identifier={'userWithProduct' + user.userId}  coordinate={{ ...user }}><ProductMarker product={selectedProduct} /></MapView.Marker>)}
            </MapView>}
          </ErrorRegion>
          <Button transparent style={styles.backButton} onPress={() => history.goBack()} >
            <Icon name='back-arrow'/>
          </Button>
        </Row>
       
      </Grid>
      <Button style={styles.nextButton} iconRight onPress={() => history.push(next)} disabled={disableDoneButton(supportsDestination, supportsOrigin)}>
        <Text uppercase={false} style={{alignSelf: 'center'}}>Continue</Text>
        <Icon name='forward-arrow' next/>
      </Button>
    </Container>;
  }
}

const validationSchema = {
  flatNumber: yup.string().max(30)
};

const styles = {
  nextButton: {
    position: 'absolute', bottom: 15, left: 15, zIndex: 2,
    width: shotgun.deviceWidth - 45,
    marginLeft: 8,
    alignItems: 'center',
    alignSelf: 'stretch',
    flexDirection: 'row',
    justifyContent: 'center'
  },
  backButton: {
    position: 'absolute',
    left: 0,
    top: 6
  },
  locationTextPlaceholder: {
    color: shotgun.silver
  },
  inputRow: {
    width: shotgun.deviceWidth - 45,
    margin: 8,
    backgroundColor: shotgun.brandPrimary,
    paddingTop: 16,
    paddingBottom: 16,
    paddingLeft: 16
  }
};

const mapStateToProps = (state, initialProps) => {
  const {delivery, selectedContentType, selectedProduct} = initialProps;
  const {destination, origin, distance, duration} = delivery;
  const showDirections = origin.line1 !== undefined && destination.line1 !== undefined;
  const disableDoneButton = (supportsDestination, supportsOrigin) => origin.line1 == undefined || supportsDestination && !distance || supportsDestination && !duration || (supportsDestination && destination.line1 == undefined) || (!supportsDestination && !supportsOrigin) || (origin.latitude && origin.longitude && origin.longitude == destination.longitude);

  return {
    ...initialProps,
    state,
    me: getDaoState(state, ['user'], 'userDao'),
    delivery, selectedProduct, selectedContentType, destination, origin, showDirections, disableDoneButton,
    usersWithProduct: getDaoState(state, ['users'], 'userRelationshipDao') || []
  };
};

export default withExternalState(mapStateToProps)(DeliveryMap);


