import React, {Component} from 'react';
import {Dimensions, Image} from 'react-native';
import {connect} from 'custom-redux';
import {Container, Button, Text, Grid, Col, Row} from 'native-base';
import MapView from 'react-native-maps';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getOperationError} from 'common/dao';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {callDriver} from 'customer/actions/CustomerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import {LoadingScreen, RatingAction, ErrorRegion, Icon, AverageRating} from 'common/components';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import locationImg from 'common/assets/location.png';

const {width, height} = Dimensions.get('window');
const ASPECT_RATIO = width / height;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

class CustomerOrderInProgress extends Component{
  constructor(props) {
    super(props);
  }

  beforeNavigateTo(){
    const {dispatch, orderId} = this.props;
    dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
      orderId,
      reportId: 'customerOrderSummary'
    }));
  }

  render() {
    let map;
    const {orderSummary = {status: ''}, history, busy, client, dispatch, errors, parentPath} = this.props;
    const {delivery = {}, contentType} = orderSummary;
    const driverPosition = {latitude: delivery.driverLatitude, longitude: delivery.driverLongitude};
    const {origin = {}, destination = {}} = delivery;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;

    const fitMap = () => {
      if ((origin.line1 !== undefined && destination.line1 !== undefined)) {
        map.fitToElements(false);
      }
    };

    const onPressCallDriver = async () => {
      dispatch(callDriver(orderSummary.orderId));
    };

    const region = {
      latitude: driverPosition.latitude,
      longitude: driverPosition.longitude,
      latitudeDelta: LATITUDE_DELTA,
      longitudeDelta: LONGITUDE_DELTA
    };

    return busy ? <LoadingScreen text="Loading Order"/> : <Container style={{flex: 1}}>
      <Grid>
        <Row size={60}>
          <MapView ref={c => { map = c; }} style={{ flex: 1 }} onMapReady={fitMap} initialRegion={region}
            showsUserLocation={false} showsBuidlings={false} showsPointsOfInterest={false} toolbarEnabled={false} showsMyLocationButton={false}>
            {contentType.destination ? <MapViewDirections client={client} locations={[origin, destination]} strokeWidth={3} /> : null}
            <MapView.Marker image={locationImg} coordinate={{...driverPosition}}/>
            <MapView.Marker coordinate={{...origin}}><AddressMarker address={origin.line1}/></MapView.Marker>
            {contentType.destination ? <MapView.Marker coordinate={{...destination}}><AddressMarker address={destination.line1}/></MapView.Marker> : null}
          </MapView>
          <Button transparent style={styles.backButton} onPress={() => history.push({pathname: `${parentPath}/CustomerOrders`, transition: 'right'})}>
            <Icon name='back-arrow'/>
          </Button>
        </Row>
        <Row size={40} style={styles.infoRow}>
          {isComplete ?
            <Col>
              <Row>
                <Col>
                  <RatingAction isDriver={false} orderSummary={orderSummary}/>
                </Col>
              </Row>
              <Row><Col style={{justifyContent: 'flex-end'}}><Button fullWidth disabled={orderSummary.driverRating == 0} onPress={()=> history.push(`${parentPath}/CustomerOrders`)}><Text uppercase={false}>Done</Text></Button></Col></Row>
            </Col> :
            <Col>
              <Grid>
                <Col>
                  <Row>
                    <Col>
                      <Text style={styles.subTitle}>Your driver</Text>
                      <Text style={styles.data}>{delivery.driverFirstName} {delivery.driverLastName}</Text>
                      <AverageRating rating={delivery.driverRatingAvg}/>
                    </Col>
                    <Col>
                      <Image source={{uri: delivery.driverImageUrl}} resizeMode='contain' style={styles.driverImage}/>
                    </Col>
                  </Row>
                </Col>
                <Col>
                  <Row>
                    <Col>
                      <Text style={styles.subTitle}>Vehicle</Text>
                      <Text style={styles.data}>{delivery.vehicleMake} {delivery.vehicleModel}, {delivery.vehicleColour}</Text>
                      <Text style={styles.data}>{delivery.registrationNumber}</Text>
                    </Col>
                  </Row>
                </Col>
              </Grid>
              <ErrorRegion errors={errors}>
                <Button fullWidth callButtonSml onPress={onPressCallDriver}>
                  <Icon name="phone" paddedIcon/>
                  <Text uppercase={false}>Call driver</Text>
                </Button>
              </ErrorRegion>
            </Col>
          }
        </Row>
      </Grid>
    </Container>;
  }
}

const styles = {
  backButton: {
    position: 'absolute',
    left: 0,
    top: 0
  },
  infoRow: {
    padding: shotgun.contentPadding
  },
  subTitle: {
    color: shotgun.brandLight,
    fontSize: 12,
    paddingBottom: 5
  },
  data: {
    fontWeight: 'bold'
  },
  navigateButton: {
    borderTopRightRadius: 0,
    borderBottomRightRadius: 0,
  },
  driverImage: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 60,
    marginRight: 10
  }
};

const findOrderSummaryFromDao = (state, orderId, daoName) => {
  const orderSummaries = getDaoState(state, ['orders'], daoName) || [];
  return  orderSummaries.find(o => o.orderId == orderId);
};

const mapStateToProps = (state, initialProps) => {
  const orderId = initialProps.location && initialProps.location.state ? initialProps.location.state.orderId : undefined;
  let orderSummary = findOrderSummaryFromDao(state, orderId, 'orderSummaryDao');
  orderSummary = orderSummary || findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');
  return {
    ...initialProps,
    orderId,
    errors: getOperationError(state, 'customerDao', 'callDriver' ),
    busy: isAnyOperationPending(state, [{ orderSummaryDao: 'resetSubscription'}, {userDao: 'getCurrentPosition'}]) || orderSummary == undefined,
    orderSummary
  };
};

mapStateToProps.dependsOnOwnProps = true;

export default connect(
  mapStateToProps
)(CustomerOrderInProgress);

