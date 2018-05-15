import React, {Component} from 'react';
import {Image} from 'react-native';
import {View, Text, List, ListItem, Row, Content} from 'native-base';
import MapViewStatic from './maps/MapViewStatic';
import moment from 'moment';
import {Icon, OriginDestinationSummary, UserInfo} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';
import {connect} from 'custom-redux';
import * as ContentTypes from 'common/constants/ContentTypes';

class OrderSummary extends Component{
  constructor(props){
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  renderMap = () => {
    const {order = {}, client, hideMap} = this.props;
    if (hideMap == true){
      return null;
    }
    const {origin, destination} = order;
    const mapWidth = shotgun.deviceWidth - 50;
    const mapHeight = mapWidth / 2;
    return <ListItem style={styles.mapListItem}>
      <MapViewStatic client={client} width={mapWidth} height={mapHeight} origin={origin} destination={destination}/>
    </ListItem>;
  }

  render() {
    const {order, userCreatedThisOrder, dispatch} = this.props;
    const {assignedPartner, customer, requiredDate} = order;

    return <Content><List>
      {assignedPartner || !userCreatedThisOrder ? <ListItem paddedLeftRight paddedTop last>
        <UserInfo dispatch={dispatch} user={userCreatedThisOrder ? {...assignedPartner, userId: assignedPartner.partnerId} : customer}/>
      </ListItem> : null}
      {order.justForFriends ? <ListItem padded><Icon paddedIcon name="one-person"/><Text>Job is visible just to friends</Text></ListItem> : null}
      {this.renderMap()}
      <ListItem padded>
        <OriginDestinationSummary order={order}/>
      </ListItem>

      {requiredDate ? <ListItem padded><Icon paddedIcon name="delivery-time"/><Text>{moment(requiredDate).format('dddd Do MMMM, h:mma')}</Text></ListItem> : null}

      <ListItem padded last>
        <Text>{order.description}</Text>
        {order.imageUrl !== undefined && order.imageUrl !== '' ?  <Row style={{justifyContent: 'center'}}><Image source={{uri: order.imageUrl}} resizeMode='contain' style={styles.image}/></Row> : null}
      </ListItem>
    </List></Content>;
  }
}

const styles = {
  mapListItem: {
    justifyContent: 'center',
    borderBottomWidth: 0,
    marginTop: 20
  },
  image: {
    aspectRatio: 1.2,
    borderRadius: 4,
    height: 180,
    marginTop: 25
  },
  itemDetailsTitle: {
    color: shotgun.brandLight,
    paddingBottom: 20,
    width: '100%'
  }
};

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
property('PageTitle', () => 'Item Details').
personell(() => 'Job Description').
rubbish(() => 'Rubbish Details')
/*eslint-enable */

const mapStateToProps = (state, initialProps) => {
  const {order} = initialProps;
  const {orderContentTypeId} = order;
  return {
    orderContentTypeId,
    ...initialProps
  };
};

const ConnectedOrderSummary = connect(
  mapStateToProps
)(OrderSummary);

export {ConnectedOrderSummary as OrderSummary};


