import React, {Component} from 'react';
import {Dimensions} from 'react-native';
import {connect} from 'react-redux';
import {Container, Button, Text, Grid, Col, Row} from 'native-base';
import MapView from 'react-native-maps';
import {updateSubscriptionAction, getDaoState, isAnyOperationPending, getOperationError} from 'common/dao';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import Products from 'common/constants/Products';
import {callDriver} from 'customer/actions/CustomerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import {withRouter} from 'react-router';
import LoadingScreen from 'common/components/LoadingScreen';
import RatingAction from 'common/components/RatingAction';
import MapViewDirections from 'common/components/maps/MapViewDirections';
import ErrorRegion from 'common/components/ErrorRegion';
import {Icon} from 'common/components/Icon';
import locationImg from 'common/assets/location.png';

const {width, height} = Dimensions.get('window');
const ASPECT_RATIO = width / height;
const LATITUDE_DELTA = 0.0322;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

class CustomerOrderInProgress extends Component{
  constructor(props) {
    super(props);
  }

  componentWillMount(){
    const {dispatch, orderId, userId} = this.props;
    dispatch(updateSubscriptionAction('orderSummaryDao', {
      userId,
      isCompleted: '',
      orderId,
      reportId: 'customerOrderSummary'
    }));
  }

  render() {
    let map;
    const {orderSummary = {status: ''}, history, busy, client, dispatch, errors} = this.props;
    const {orderItem = {}, delivery = {}} = orderSummary;
    const driverPosition = {latitude: delivery.driverLatitude, longitude: delivery.driverLongitude};
    const {origin = {}, destination = {}, driverRating} = delivery;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;
    const isDelivery = orderItem.productId == Products.DELIVERY;

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
            {isDelivery ? <MapViewDirections client={client} locations={[origin, destination]} strokeWidth={3} /> : null}
            <MapView.Marker image={locationImg} coordinate={{...driverPosition}}/>
            <MapView.Marker coordinate={{...origin}}><AddressMarker address={origin.line1}/></MapView.Marker>
            {isDelivery ? <MapView.Marker coordinate={{...destination}}><AddressMarker address={destination.line1}/></MapView.Marker> : null}
          </MapView>
          <Button transparent style={styles.backButton}>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Row>
        <Row size={40} style={styles.infoRow}>
          {isComplete ?
            <Col>
              <Row>
                <Col>
                  <RatingAction isDriver={false} delivery={delivery}/>
                </Col>
              </Row>
              <Row><Col style={{justifyContent: 'flex-end'}}><Button fullWidth disabled={driverRating == -1} onPress={()=> history.push('/Customer')}><Text uppercase={false}>Done</Text></Button></Col></Row>
            </Col> :
            <Col>
              <Grid>
                <Row>
                  <Col>
                    <Row><Text>Driver photo here</Text></Row>
                    <Row><Text>Driver Avg Rating Here</Text></Row>
                  </Col>
                  <Col>
                    <Row>
                      <Col>
                        <Text style={styles.subTitle}>Your delivery driver</Text>
                        <Text style={styles.data}>{delivery.driverFirstName} {delivery.driverLastName}</Text>
                      </Col>
                    </Row>
                    <Row>
                      <Col>
                        <Text style={styles.subTitle}>Vehicle</Text>
                        <Text style={styles.data}>{delivery.vehicleMake} {delivery.vehicleModel}, {delivery.vehicleColour}</Text>
                        <Text style={styles.data}>{delivery.registrationNumber}</Text>
                      </Col>
                    </Row>
                  </Col>
                </Row>
              </Grid>
              <ErrorRegion errors={errors}>
                <Button fullWidth callButton onPress={onPressCallDriver}>
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
  }
};

const mapStateToProps = (state, initialProps) => {
  const orderId = initialProps.location && initialProps.location.state ? initialProps.location.state.orderId : undefined;
  const orderSummaries = getDaoState(state, ['orders'], 'orderSummaryDao') || [];
  const orderSummary = orderSummaries.find(o => o.orderId == orderId);

  return {
    ...initialProps,
    orderId,
    errors: getOperationError(state, 'customerDao', 'callDriver' ),
    busy: isAnyOperationPending(state, [{ orderSummaryDao: 'updateSubscription'}, {userDao: 'getCurrentPosition'}]) || orderSummary == undefined,
    orderSummary
  };
};

mapStateToProps.dependsOnOwnProps = true;

export default withRouter(connect(
  mapStateToProps
)(CustomerOrderInProgress));

