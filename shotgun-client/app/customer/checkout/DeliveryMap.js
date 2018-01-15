import React, { Component } from 'react';
import { Dimensions } from 'react-native';
import Products from 'common/constants/Products';
import { connect } from 'custom-redux';
import { Container, Button, Text, Icon, Grid, Col, Row } from 'native-base';
import MapView from 'react-native-maps';
import LoadingScreen from 'common/components/LoadingScreen';
import AddressMarker from 'common/components/maps/AddressMarker';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import { withRouter } from 'react-router';
import { getDaoState, isAnyOperationPending } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';

const { width, height } = Dimensions.get('window');
const ASPECT_RATIO = width / height;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;
const EMPTY_LOCATION = {
  flatNumber: undefined,
  line1: undefined,
  city: undefined,
  postCode: undefined,
  latitude: undefined,
  longitude: undefined
};


const mapFitCoordinates = {
  edgePadding: {
    right: Math.round(width / 20) + 100,
    bottom: Math.round(height / 20),
    left: Math.round(width / 20) + 100,
    top: Math.round(height / 20) + 300,
  }
};

const getLocationText = (location, place, placeholder, doAddressLookup) => {
  const style = location.line1 ? styles.locationText : styles.locationTextPlaceholder;
  const text = location.line1 ? `${location.line1}, ${location.postCode}` : placeholder;
  return <Text style={style} onPress={() => doAddressLookup(place, placeholder)}>{text}</Text>;
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
  locationText: {

  },
  inputRow: {
    padding: shotgun.contentPadding,
    shadowOffset: { width: 10, height: 10, },
    shadowColor: 'black',
    shadowOpacity: 1.0/*,
    elevation: 3*/
  }
};


class DeliveryMap extends Component {
  constructor(props) {
    super(props);
    this.doAddressLookup = this.doAddressLookup.bind(this);
    this.fitMap = this.fitMap.bind(this);
  }

  clearLocation(type) {
    context.setState({ [type]: EMPTY_LOCATION });
  }


  doAddressLookup(addressKey, addressLabel) {
    const { history } = this.props;
    history.push('/Customer/Checkout/AddressLookup', { addressKey, addressLabel });
  }

  fitMap(result) {
    let {  position } = this.props;
    const { context } = this.props;
    const { destination = EMPTY_LOCATION, origin = EMPTY_LOCATION } = context.state;
    if ((origin !== EMPTY_LOCATION && destination !== EMPTY_LOCATION)) {
      this.map.fitToSuppliedMarkers(['origin', 'destination'], false);
      return;
    }
    this.map.fitToCoordinates(result.coordinates, mapFitCoordinates);
    
    if (origin !== EMPTY_LOCATION) {
      position = origin;
    } else if (destination !== EMPTY_LOCATION) {
      position = destination;
    }

    const { latitude, longitude } = position;
    this.map.animateToCoordinate({ latitude, longitude }, 1);
  }

  render() {
    const { history, context, client, busy } = this.props;
    let { position } = this.props;
    const {orderItem, delivery} = context.state;
    const {destination = EMPTY_LOCATION, origin = EMPTY_LOCATION} = delivery;
    const showDirections = origin.line1 !== undefined && destination.line1 !== undefined;
    const disableDoneButton = origin.line1 == undefined || (orderItem.productId == Products.DELIVERY && destination.line1 == undefined);
    const isDelivery = orderItem.productId == Products.DELIVERY;

    if ((origin === EMPTY_LOCATION || destination === EMPTY_LOCATION)) {
      if (origin !== EMPTY_LOCATION) {
        position = origin;
      } else if (destination !== EMPTY_LOCATION) {
        position = destination;
      }
    }


    const region = {
      latitude: position.latitude,
      longitude: position.longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

    return busy ? <LoadingScreen text="Loading Map" /> : <Container style={{ flex: 1 }}>
      <Grid>
        <Row size={85}>
          <MapView ref={c => { this.map = c; }} style={{ flex: 1 }} showsUserLocation={true} showsMyLocationButton={true} initialRegion={region}>
            {showDirections ?
              <MapViewDirections client={client} origin={{ latitude: origin.latitude, longitude: origin.longitude }}
                destination={{ latitude: destination.latitude, longitude: destination.longitude }}
                strokeWidth={3} onReady={(result) => { this.fitMap(result); }} /> : null}

            {origin.line1 ? <MapView.Marker identifier="origin" coordinate={{ ...origin }}><AddressMarker address={origin.line1} /></MapView.Marker> : null}
            {destination.line1 ? <MapView.Marker identifier="destination" coordinate={{ ...destination }}><AddressMarker address={destination.line1} /></MapView.Marker> : null}
          </MapView>
          <Button transparent style={styles.backButton}>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Row>
        <Row size={15} style={styles.inputRow}>
          <Col>
            <Row>
              <Icon name="pin" paddedIcon originPin />
              {getLocationText(origin, 'origin', 'Enter pick-up location', this.doAddressLookup)}
            </Row>
            {isDelivery ? <Row>
              <Icon name="pin" paddedIcon />
              {getLocationText(destination, 'destination', 'Enter drop-off location', this.doAddressLookup)}
            </Row> : null}
          </Col>
        </Row>
      </Grid>
      <Button fullWidth paddedBottom iconRight onPress={() => history.push('/Customer/Checkout/DeliveryOptions')} disabled={disableDoneButton}>
        <Text uppercase={false}>Continue</Text>
        <Icon name='arrow-forward' />
      </Button>
    </Container>;
  }
}


const mapStateToProps = (state, initialProps) => ({
  state,
  position: getDaoState(state, ['position'], 'userDao'),
  busy: isAnyOperationPending(state, { userDao: 'getCurrentPosition' }),
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(DeliveryMap));


