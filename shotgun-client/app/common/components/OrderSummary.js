import React, {Component} from 'react';
import {Dimensions, Image} from 'react-native';
import {Text, List, ListItem, Grid, Row} from 'native-base';
import MapViewStatic from './maps/MapViewStatic';
import moment from 'moment';
import {Icon, OriginDestinationSummary} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';
import {connect} from 'custom-redux';
import * as ContentTypes from 'common/constants/ContentTypes';

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', () => 'Item Details').
    personell(() => 'Job Description').
    rubbish(() => 'Rubbish Details')
    /*eslint-disable */

class OrderSummary extends Component{
  constructor(props){
    super(props);
    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);
    this.renderItemDetails = this.renderItemDetails.bind(this);
  }

  componentWillReceiveProps(newProps){
    ContentTypes.resolveResourceFromProps(newProps, resourceDictionary, this);
  }

  renderMap(){
    const { width } = Dimensions.get('window');
    const {delivery = {}, client} = this.props;
    const {origin, destination} = delivery;
    const mapWidth = width - 50;
    const mapHeight = mapWidth / 2;
    return <ListItem style={styles.mapListItem}>
      <MapViewStatic client={client} width={mapWidth} height={mapHeight} origin={origin} destination={destination}/>
    </ListItem>;
  }

  renderItemDetails(){
    const {orderItem} = this.props;
    const {resources} = this;
    orderItem.imageUrl = orderItem.imageData !== undefined ? `data:image/jpeg;base64,${orderItem.imageData}` : orderItem.imageUrl;
    return <ListItem padded style={{borderBottomWidth: 0}}>
      <Grid>
        <Row><Text style={styles.itemDetailsTitle}>{resources.PageTitle()}</Text></Row>
        <Row><Text>{orderItem.notes}</Text></Row>
        {orderItem.imageUrl !== undefined && orderItem.imageUrl !== '' ?  <Row style={{justifyContent: 'center'}}><Image source={{uri: orderItem.imageUrl}} resizeMode='contain' style={styles.image}/></Row> : null}
      </Grid>
    </ListItem>;
  }

  render() {
    const {orderItem, delivery, contentType, product, deliveryUser} = this.props;
    const {quantity: noPeople} = orderItem;
    
    return <List>
      {this.renderMap()}
      <ListItem padded>
        <OriginDestinationSummary contentType={contentType} delivery={delivery}/>
      </ListItem>
      {deliveryUser ? <ListItem padded><Icon paddedIcon name="one-person"/><Text>{`Assigned to ${deliveryUser.firstName} ${deliveryUser.lastName}`}</Text></ListItem> : null}
      {contentType.fromTime ? <ListItem padded><Icon paddedIcon name="delivery-time"/><Text>{moment(delivery.from).format('dddd Do MMMM, h:mma')}</Text></ListItem> : null}
      {contentType.tillTime ? <ListItem padded><Icon paddedIcon name="delivery-time"/><Text>{moment(delivery.till).format('dddd Do MMMM, h:mma')}</Text></ListItem> : null}
      {contentType.noPeople && noPeople ? <ListItem padded><Icon paddedIcon name="one-person"/><Text key='text'>{`${noPeople} people required`}</Text></ListItem> : null}
      {product ? <ListItem padded>
        {product.imageUrl ? <Icon paddedIcon name={product.imageUrl}/> : null}
        <Text>{`${product.name}`}</Text>
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

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps
  };
};

const ConnectedOrderSummary = connect(
  mapStateToProps
)(OrderSummary);

export {ConnectedOrderSummary as OrderSummary};


