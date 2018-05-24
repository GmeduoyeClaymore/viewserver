import React, {Component} from 'react';
import {Image} from 'react-native';
import {Text, List, ListItem, Row, Content, View} from 'native-base';
import MapViewStatic from './maps/MapViewStatic';
import {Icon, OriginDestinationSummary, UserInfo, VehicleInfo} from 'common/components';
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

  onUserInfoPress = (userForInfo) => {
    const {ordersRoot, history} = this.props;

    history.push({
      pathname: `${ordersRoot}/UserDetail`,
      state: {userId: userForInfo.userId},
      transition: 'bottom'
    });
  }

  render() {
    const {order, userCreatedThisOrder, dispatch} = this.props;
    const {assignedPartner, customer, negotiatedOrderStatus, orderProduct, vehicle} = order;
    const isRejected = negotiatedOrderStatus == 'REJECTED';
    const userForInfo = userCreatedThisOrder ? assignedPartner ? {...assignedPartner, userId: assignedPartner.partnerId} : undefined : customer;

    return <Content><List>
      {!isRejected && (userForInfo) ? <ListItem paddedLeftRight paddedTopBottom onPress={() => this.onUserInfoPress(userForInfo)}>
        <UserInfo dispatch={dispatch} user={userForInfo}/>
      </ListItem> : null}
      {order.justForFriends ? <ListItem padded><Icon paddedIcon name="one-person"/><Text>Job is visible just to friends</Text></ListItem> : null}

      {assignedPartner && userCreatedThisOrder && vehicle ? <ListItem paddedLeftRight paddedTop last>
        <VehicleInfo order={order}/>
      </ListItem> : null}

      {orderProduct ? <ListItem padded style={styles.productInfoRow}>
        <Row>
          {orderProduct.imageUrl ? <Icon paddedIcon name={orderProduct.imageUrl}/> : null}
          <Icon paddedIcon name={orderProduct.productId}/>
          <Text>{`${orderProduct.name}`}</Text>
        </Row>
        <View>
          <Text style={styles.title}>{order.title}</Text>
          {order.description ? <Text style={styles.description}>{order.description}</Text> : null}
        </View>
      </ListItem> : null}

      <ListItem padded last>
        <OriginDestinationSummary order={order}/>
      </ListItem>

      {this.renderMap()}

      <ListItem padded last>
        {order.imageUrl !== undefined && order.imageUrl !== '' ?  <Row style={{justifyContent: 'center'}}><Image source={{uri: order.imageUrl}} resizeMode='contain' style={styles.image}/></Row> : null}
        {order.imageData !== undefined && order.imageData !== '' ?  <Row style={{justifyContent: 'center'}}><Image source={{uri: `data:image/jpeg;base64,${order.imageData}`}} resizeMode='contain' style={styles.image}/></Row> : null}
      </ListItem>

    </List></Content>;
  }
}

const styles = {
  mapListItem: {
    justifyContent: 'center',
    paddingBottom: shotgun.contentPadding
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
  },
  productInfoRow: {
    flexDirection: 'column',
    alignItems: 'flex-start'
  },
  title: {
    paddingTop: 10
  },
  description: {
    fontStyle: 'italic',
    color: shotgun.brandLight,
    paddingTop: 5
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


