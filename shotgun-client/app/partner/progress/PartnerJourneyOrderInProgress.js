import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Linking} from 'react-native';
import {Button, Text, View, ListItem} from 'native-base';
import {SpinnerButton, OriginDestinationSummary} from 'common/components';
import {getDaoState, isAnyOperationPending} from 'common/dao';
import {startJourney, completeJourney} from 'partner/actions/PartnerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import MapViewStatic from 'common/components/maps/MapViewStatic';
import Logger from 'common/Logger';
import * as ContentTypes from 'common/constants/ContentTypes';

const CAN_START_JOURNEY_STATUSES = ['PENDINGSTART'];
const CAN_COMPLETE_JOURNEY_STATUSES = ['ENROUTE'];

class PartnerJourneyOrderInProgress extends Component{
  constructor(props){
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  onNavigatePress = async() => {
    const {order, user} = this.props;
    const {latitude, longitude} = user;
    const {origin, destination} = order;
    const navigationDestination = destination && destination.line1 !== undefined ? destination : origin;
    const wayPoints = destination && destination.line1 !== undefined ? `&waypoints=${origin.latitude},${origin.longitude}` : undefined;
    const mapUrl = `https://www.google.com/maps/dir/?api=1&travelmode=driving&dir_action=navigate${wayPoints}&origin=${latitude},${longitude}&destination=${navigationDestination.latitude},${navigationDestination.longitude}`;
    try {
      const isSupported = await Linking.canOpenURL(mapUrl);

      if (isSupported) {
        return Linking.openURL(mapUrl);
      }
      throw new Error('Could not open');
    } catch (e) {
      Logger.warning(`Could not open ${mapUrl}`);
    }
  };

  fitMap = () => {
    map.fitToElements(false);
  };

  onJourneyStart = async() => {
    const {order, dispatch} = this.props;
    dispatch(startJourney(order.orderId, order.orderContentTypeId));
  };

  onJourneyComplete = async() => {
    const {order, dispatch} = this.props;
    dispatch(completeJourney(order.orderId, order.orderContentTypeId));
  };

  render() {
    const {order = {}, client, busyUpdating} = this.props;
    const {origin, destination = {}} = order;
    const {resources} = this;
    const {StartButtonCaption = 'Start Job', StopButtonCaption = 'Stop Job'} = resources;
    
    const mapWidth = shotgun.deviceWidth - 50;
    const mapHeight = mapWidth / 2;

    return <View>
      {!!~CAN_START_JOURNEY_STATUSES.indexOf(order.journeyOrderStatus) ? <SpinnerButton busy={busyUpdating} fullWidth padded onPress={this.onJourneyStart}><Text uppercase={false}>{StartButtonCaption}</Text></SpinnerButton> : null}
      {!!~CAN_COMPLETE_JOURNEY_STATUSES.indexOf(order.journeyOrderStatus) ? <Button fullWidth padded style={styles.navigationButton} onPress={this.onNavigatePress}><Text uppercase={false}>Show navigation</Text></Button> : null}
      {!!~CAN_COMPLETE_JOURNEY_STATUSES.indexOf(order.journeyOrderStatus) ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.completeButton} onPress={this.onJourneyComplete}><Text uppercase={false}>{StopButtonCaption}</Text></SpinnerButton> : null}
      <ListItem padded>
        <OriginDestinationSummary order={order}/>
      </ListItem>
      <ListItem padded last>
        <MapViewStatic client={client} width={mapWidth} height={mapHeight} origin={origin} destination={destination}/>
      </ListItem>
    </View>;
  }
}

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('StartButtonCaption', 'Start Journey').
    rubbish('Start Collection').
  property('StopButtonCaption', 'Complete Journey').
    rubbish('Complete Collection');
/*eslint-enable */

const styles = {
  navigationButton: {
    marginBottom: 15
  }
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps,
    user: getDaoState(state, ['user'], 'userDao'),
    busyUpdating: isAnyOperationPending(state, [{ orderDao: 'startJourney'}, { orderDao: 'completeJourney'}])
  };
};

export default connect(mapStateToProps)(PartnerJourneyOrderInProgress);

