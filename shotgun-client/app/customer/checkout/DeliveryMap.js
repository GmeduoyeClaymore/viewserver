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

const ASPECT_RATIO = shotgun.deviceWidth / shotgun.deviceHeight;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

const DeliveryMap = ({history, context, client, busy, position}) => {
  let map;
  const {orderItem, delivery} = context.state;
  const {destination, origin} = delivery;
  const showDirections = origin.line1 !== undefined && destination.line1 !== undefined;
  const disableDoneButton = origin.line1 == undefined || (orderItem.productId == Products.DELIVERY && destination.line1 == undefined);
  const isDelivery = orderItem.productId == Products.DELIVERY;

  if (origin.line1 !== undefined) {
    position = origin;
  } else if (destination.line1 !== undefined) {
    position = destination;
  }

  const {latitude, longitude} = position;

  const initialRegion = {
    latitude,
    longitude,
    latitudeDelta: LATITUDE_DELTA,
    longitudeDelta: LONGITUDE_DELTA
  };

  const getLocationText = (location, place, placeholder, doAddressLookup) => {
    const style = location.line1 ? {} : styles.locationTextPlaceholder;
    const text = location.line1 ? `${location.line1}, ${location.postCode}` : placeholder;
    return <Text style={style} onPress={() => doAddressLookup(place, placeholder)}>{text}</Text>;
  };

  const doAddressLookup = (addressKey, addressLabel) => {
    history.push('/Customer/Checkout/AddressLookup', { addressKey, addressLabel });
  };

  const fitMap = () => {
    if ((origin.line1 !== undefined && destination.line1 !== undefined)) {
      map.fitToElements(false);
    }
  };

  return busy ? <LoadingScreen text="Loading Map" /> : <Container style={{ flex: 1 }}>
    <Grid>
      <Row size={85}>
        <MapView ref={c => { map = c; }} style={{ flex: 1 }} onMapReady={fitMap} initialRegion={initialRegion}
          showsUserLocation={true} showsBuidlings={false} showsPointsOfInterest={false} showsMyLocationButton={true}>
          {showDirections ?
            <MapViewDirections client={client} origin={{ latitude: origin.latitude, longitude: origin.longitude }}
              destination={{ latitude: destination.latitude, longitude: destination.longitude }}
              strokeWidth={3} /> : null}

          {origin.line1 ? <MapView.Marker identifier="origin" coordinate={{ ...origin }}><AddressMarker address={origin.line1} /></MapView.Marker> : null}
          {destination.line1 ? <MapView.Marker identifier="destination" coordinate={{ ...destination }}><AddressMarker address={destination.line1} /></MapView.Marker> : null}
        </MapView>
        <Button transparent style={styles.backButton}>
          <Icon name='arrow-back' onPress={() => history.push('/Customer/Checkout/ProductSelect')} />
        </Button>
      </Row>
      <Row size={15} style={styles.inputRow}>
        <Col>
          <Row>
            <Icon name="pin" paddedIcon originPin />
            {getLocationText(origin, 'origin', 'Enter pick-up location', doAddressLookup)}
          </Row>
          {isDelivery ? <Row>
            <Icon name="pin" paddedIcon />
            {getLocationText(destination, 'destination', 'Enter drop-off location', doAddressLookup)}
          </Row> : null}
        </Col>
      </Row>
    </Grid>
    <Button fullWidth paddedBottom iconRight onPress={() => history.push('/Customer/Checkout/DeliveryOptions')} disabled={disableDoneButton}>
      <Text uppercase={false}>Continue</Text>
      <Icon name='arrow-forward' />
    </Button>
  </Container>;
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
    padding: shotgun.contentPadding,
    shadowOffset: { width: 10, height: 10, },
    shadowColor: 'black',
    shadowOpacity: 1.0
  }
};

const mapStateToProps = (state, initialProps) => {
  const position = getDaoState(state, ['position'], 'userDao');
  return {
    ...initialProps,
    state,
    position,
    busy: isAnyOperationPending(state, { userDao: 'getCurrentPosition' }) || !position,
  };
};

export default withRouter(connect(
  mapStateToProps
)(DeliveryMap));


