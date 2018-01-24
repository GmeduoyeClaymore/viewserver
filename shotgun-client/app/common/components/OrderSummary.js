import React, {Component} from 'react';
import {Dimensions, Image} from 'react-native';
import {Text, List, ListItem, Icon, Grid, Row} from 'native-base';
import MapViewStatic from './maps/MapViewStatic';
import moment from 'moment';
import LoadingScreen from 'common/components/LoadingScreen';
import Products from 'common/constants/Products';
import shotgun from 'native-base-theme/variables/shotgun';
import {connect} from 'custom-redux';
import { getDaoState, isAnyOperationPending } from 'common/dao';

class OrderSummary extends Component{
  constructor(){
    super();
  }

  render() {
    const { width } = Dimensions.get('window');
    const {orderItem, delivery, client, busy, selectedVehicleType, contentType, product} = this.props;
    const {origin, destination} = delivery;
    const {quantity: noPeople} = orderItem;
    const mapWidth = width - 50;
    const mapHeight = mapWidth / 2;

    orderItem.imageUrl = orderItem.imageData !== undefined ? `data:image/jpeg;base64,${orderItem.imageData}` : orderItem.imageUrl;

    return busy ? <LoadingScreen text="Loading Vehicle Types" /> : <List>
      <ListItem style={styles.mapListItem}>
        <MapViewStatic client={client} width={mapWidth} height={mapHeight} origin={origin} destination={destination}/>
      </ListItem>

      <ListItem padded>
        <Grid>
          {contentType.origin ? <Row><Icon name="pin" paddedIcon originPin/><Text>{origin.line1}, {origin.postCode}</Text></Row> : null}
          {delivery.duration ? <Row><Text time>| {delivery.duration} hrs</Text></Row> : null}
          {contentType.destination ? <Row><Icon paddedIcon name="pin"/><Text>{destination.line1}, {destination.postCode}</Text></Row> : null}
        </Grid>
      </ListItem>
      {contentType.fromTime ? <ListItem padded><Icon paddedIcon name="time"/><Text>{moment(delivery.from).format('dddd Do MMMM, h:mma')}</Text></ListItem> : null}
      {contentType.tillTime ? <ListItem padded><Icon paddedIcon name="time"/><Text>{moment(delivery.till).format('dddd Do MMMM, h:mma')}</Text></ListItem> : null}

      {contentType.noPeople > 0 ? <ListItem padded>
        <Icon key='icon' paddedIcon name="man"/><Text key='text'>{`${noPeople} people required`}</Text>
      </ListItem> : null}

      {selectedVehicleType ? <ListItem padded>
        <Icon paddedIcon name='car'/><Text key='text'>{`${selectedVehicleType.description}`}</Text>
      </ListItem> : null}

      {product ? <ListItem padded>
        <Image source={{uri: 'https://media.istockphoto.com/vectors/minimalistic-solid-line-colored-builder-icon-vector-id495391344?k=6&m=495391344&s=612x612&w=0&h=SFsgxOa-pdm9NTbc3NVj-foksXnqyPW3LhNjJtQLras='}} style={styles.picture} />
        <Text key='text'>{`${product.name}`}</Text>
      </ListItem> : null}
      
      <ListItem padded style={{borderBottomWidth: 0}}>
        <Grid>
          <Row><Text style={styles.itemDetailsTitle}>Item Details</Text></Row>
          <Row><Text>{orderItem.notes}</Text></Row>
          {orderItem.imageUrl !== undefined && orderItem.imageUrl !== '' ?  <Row style={{justifyContent: 'center'}}><Image source={{uri: orderItem.imageUrl}} resizeMode='contain' style={styles.image}/></Row> : null}
        </Grid>
      </ListItem>
    </List>;
  }
}


const mapStateToProps = (state, initialProps) => {
  const vehicleTypes = getDaoState(state, ['vehicleTypes'], 'vehicleTypeDao') || [];
  const {delivery} = initialProps;
  const selectedVehicleType = vehicleTypes.find(c=> c.vehicleTypeId === delivery.vehicleTypeId);
  return {
    ...initialProps,
    selectedVehicleType,
    busy: isAnyOperationPending(state, { vehicleTypeDao: 'vehicleTypes' }) || !selectedVehicleType,
  };
};

export default connect(
  mapStateToProps
)(OrderSummary);


const styles = {
  mapListItem: {
    justifyContent: 'center',
    borderBottomWidth: 0,
    marginTop: 20
  },
  picture: {
    width: 80,
    height: 80,
    borderRadius: 20,
    marginRight: 8
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

