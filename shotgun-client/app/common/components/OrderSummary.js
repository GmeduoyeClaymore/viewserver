import React, {Component} from 'react';
import {Image} from 'react-native';
import {Text, List, ListItem, Grid, Row} from 'native-base';
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
    const {order = {}, client} = this.props;
    const {origin, destination} = order;
    const mapWidth = shotgun.deviceWidth - 50;
    const mapHeight = mapWidth / 2;
    return <ListItem style={styles.mapListItem}>
      <MapViewStatic client={client} width={mapWidth} height={mapHeight} origin={origin} destination={destination}/>
    </ListItem>;
  }

  renderItemDetails = () => {
    const {order} = this.props;
    return <ListItem padded style={{borderBottomWidth: 0}}>
      <Grid>
        <Row><Text style={styles.itemDetailsTitle}>{this.resources.PageTitle()}</Text></Row>
        <Row><Text>{order.description}</Text></Row>
        {order.imageUrl !== undefined && order.imageUrl !== '' ?  <Row style={{justifyContent: 'center'}}><Image source={{uri: order.imageUrl}} resizeMode='contain' style={styles.image}/></Row> : null}
      </Grid>
    </ListItem>;
  }

  render() {
    const {order} = this.props;
    const {assignedPartner, userCreatedThisOrder = false, customer, orderProduct, requiredDate} = order;

    return <List>
      {this.renderMap()}
      <ListItem padded>
        <OriginDestinationSummary order={order}/>
      </ListItem>

      {assignedPartner || !userCreatedThisOrder ? <ListItem padded>
        <Icon paddedIcon name="one-person"/>
        <UserInfo orderid={order.orderId} user={userCreatedThisOrder ? assignedPartner : customer} isPartner={!userCreatedThisOrder}/>
      </ListItem> : null}

      {requiredDate ? <ListItem padded><Icon paddedIcon name="delivery-time"/><Text>{moment(requiredDate).format('dddd Do MMMM, h:mma')}</Text></ListItem> : null}
      {orderProduct ? <ListItem padded>
        {orderProduct.imageUrl ? <Icon paddedIcon name={orderProduct.imageUrl}/> : null}
        <Text>{orderProduct.name}</Text>
      </ListItem> : null}
      {this.renderItemDetails()}
    </List>;
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
    marginBottom: 10
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


