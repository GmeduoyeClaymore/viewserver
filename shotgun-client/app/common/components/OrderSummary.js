import React, {Component} from 'react';
import {Dimensions, Image} from 'react-native';
import {Text, List, ListItem, Icon, Grid, Row} from 'native-base';
import MapViewStatic from './maps/MapViewStatic';
import moment from 'moment';
import Products from 'common/constants/Products';
import shotgun from 'native-base-theme/variables/shotgun';

export default class OrderSummary extends Component{
  constructor(){
    super();
  }

  render() {
    const { width } = Dimensions.get('window');
    const {orderItem, delivery, client} = this.props;
    const {origin, destination, noRequiredForOffload} = delivery;
    const mapWidth = width - 50;
    const mapHeight = mapWidth / 2;
    const isDelivery = orderItem.productId == Products.DELIVERY;

    orderItem.imageUrl = orderItem.imageData !== undefined ? `data:image/jpeg;base64,${orderItem.imageData}` : orderItem.imageUrl;

    return <List>
      <ListItem style={{justifyContent: 'center', borderBottomWidth: 0, marginTop: 20}}>
        <MapViewStatic client={client} width={mapWidth} height={mapHeight} origin={origin} destination={destination}/>
      </ListItem>

      <ListItem padded>
        <Grid>
          <Row><Icon name="pin" paddedIcon originPin/><Text>{origin.line1}, {origin.postCode}</Text></Row>
          {isDelivery ? <Row><Text time>| 3 hrs</Text></Row> : null}
          {isDelivery ? <Row><Icon paddedIcon name="pin"/><Text>{destination.line1}, {destination.postCode}</Text></Row> : null}
        </Grid>
      </ListItem>

      <ListItem padded>
        <Icon paddedIcon name="time"/><Text>{moment(delivery.eta).format('dddd Do MMMM, h:mma')}</Text>
      </ListItem>

      {delivery.noRequiredForOffload > 0 ? <ListItem padded>
        <Icon key='icon' paddedIcon name="man"/><Text key='text'>{`${noRequiredForOffload} people required`}</Text>
      </ListItem> : null}

      <ListItem padded style={{borderBottomWidth: 0}}>
        <Grid>
          <Row><Text style={styles.itemDetailsTitle}>Item Details</Text></Row>
          <Row><Text>{orderItem.notes}</Text></Row>
          {orderItem.imageUrl !== undefined && orderItem.imageUrl !== '' ?  <Row><Image source={{uri: orderItem.imageUrl}} style={styles.image}/></Row> : null}
        </Grid>
      </ListItem>
    </List>;
  }
}

const styles = {
  image: {
    flex: 1,
    height: 180,
    marginTop: 25
  },
  itemDetailsTitle: {
    color: shotgun.brandLight,
    marginBottom: 10
  }
};

