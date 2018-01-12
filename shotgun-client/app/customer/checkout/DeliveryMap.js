import React, {Component} from 'react';
import {Dimensions} from 'react-native';
import Products from 'common/constants/Products';
import {connect} from 'react-redux';
import {Container, Button, Text, Icon, Grid, Col, Row} from 'native-base';
import MapView from 'react-native-maps';
import LoadingScreen from 'common/components/LoadingScreen';
import AddressMarker from 'common/components/maps/AddressMarker';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import {withRouter} from 'react-router';
import {getDaoState, isAnyOperationPending} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';

const {width, height} = Dimensions.get('window');
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


class DeliveryMap extends Component {
  constructor(props) {
    super(props);
  }

  clearLocation(type) {
    context.setState({[type]: EMPTY_LOCATION});
  }

  updateMapRegion({latitude, longitude}) {
    setTimeout(() => this.map.animateToCoordinate({latitude, longitude}, 1), 50);
    return null;
  }

  onAddressSelected(addressKey) {
    return (details) => onLocationSelect(addressKey, details);
  }

  doAddressLookup(addressKey, addressLabel) {
    const {history} = this.props;
    history.push('/Customer/Checkout/AddressLookup', {addressKey, addressLabel});
  }
  
  render() {
    const {history, context, client, busy, position} = this.props;
    const {orderItem, destination = {}, origin = {}} = context.state;
    const showDirections = origin.line1 !== undefined && destination.line1 !== undefined;
    const disableDoneButton = origin.line1 == undefined || (orderItem.productId == Products.DELIVERY && destination.line1 == undefined);
    const isDelivery = orderItem.productId == Products.DELIVERY;

    const region = {
      latitude: position.latitude,
      longitude: position.longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

    const mapFitCoordinates = {
      edgePadding: {
        right: Math.round(width / 20) + 100,
        bottom: Math.round(height / 20),
        left: Math.round(width / 20) + 100,
        top: Math.round(height / 20) + 300,
      }
    };

    const getLocationText = (location, place, placeholder) =>  {
      const style = location.line1 ? styles.locationText : styles.locationTextPlaceholder;
      const text = location.line1 ? `${location.line1}, ${location.postCode}` : placeholder;
      return <Text style={style} onPress={() => this.doAddressLookup(place, placeholder)}>{text}</Text>;
    };

    return busy ? <LoadingScreen text="Loading Map"/> : <Container style={{flex: 1}}>
      <Grid>
        <Row size={85}>
          <MapView ref={c => {this.map = c;}} style={{flex: 1}} showsUserLocation={true} showsMyLocationButton={true} initialRegion={region}>
            {showDirections ?
              <MapViewDirections client={client} origin={{latitude: origin.latitude, longitude: origin.longitude}}
                destination={{latitude: destination.latitude, longitude: destination.longitude}}
                strokeWidth={3} onReady={(result) => {this.map.fitToCoordinates(result.coordinates, mapFitCoordinates);}}/> : null}

            {origin.line1 ? <MapView.Marker coordinate={{...origin}}><AddressMarker address={origin.line1}/></MapView.Marker> : null}
            {destination.line1 ? <MapView.Marker coordinate={{...destination}}><AddressMarker address={destination.line1}/></MapView.Marker> : null}
          </MapView>
          <Button transparent style={styles.backButton}>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Row>
        <Row size={15} style={styles.inputRow}>
          <Col>
            <Row>
              <Icon name="pin" paddedIcon originPin/>
              {getLocationText(origin, 'origin', 'Enter pick-up location')}
            </Row>
            {isDelivery ? <Row>
              <Icon name="pin" paddedIcon/>
              {getLocationText(destination, 'destination', 'Enter drop-off location')}
            </Row> : null}
          </Col>
        </Row>
      </Grid>
      <Button fullWidth paddedBottom iconRight onPress={() => history.push('/Customer/Checkout/DeliveryOptions')} disabled={disableDoneButton}>
        <Text uppercase={false}>Continue</Text>
        <Icon name='arrow-forward'/>
      </Button>
      {this.updateMapRegion(position)}
    </Container>;
  }
}

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
    shadowOffset: {width: 10, height: 10, },
    shadowColor: 'black',
    shadowOpacity: 1.0/*,
    elevation: 3*/
  }
};

const mapStateToProps = (state, initialProps) => ({
  state,
  position: getDaoState(state, ['position'], 'userDao'),
  busy: isAnyOperationPending(state, {userDao: 'getCurrentPosition'}),
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(DeliveryMap));


