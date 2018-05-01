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
/*eslint-enable */


class OrderSummary extends Component{
  constructor(props){
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
    this.renderItemDetails = this.renderItemDetails.bind(this);
  }


  renderMap(){
    const { width } = Dimensions.get('window');
    const {order = {}, client} = this.props;
    const {origin, destination} = order;
    const mapWidth = width - 50;
    const mapHeight = mapWidth / 2;
    return <ListItem style={styles.mapListItem}>
      <MapViewStatic client={client} width={mapWidth} height={mapHeight} origin={origin} destination={destination}/>
    </ListItem>;
  }

  renderItemDetails(){
    const {order} = this.props;
    const {resources} = this;
    return <ListItem padded style={{borderBottomWidth: 0}}>
      <Grid>
        <Row><Text style={styles.itemDetailsTitle}>{resources.PageTitle()}</Text></Row>
        <Row><Text>{order.description}</Text></Row>
        {order.imageUrl !== undefined && order.imageUrl !== '' ?  <Row style={{justifyContent: 'center'}}><Image source={{uri: order.imageUrl}} resizeMode='contain' style={styles.image}/></Row> : null}
      </Grid>
    </ListItem>;
  }

  render() {
    const {order} = this.props;
    const {partnerUser, orderProduct, noPeopleRequired, requiredDate} = order;
    
    return <List>
      {this.renderMap()}
      <ListItem padded>
        <OriginDestinationSummary {...order}/>
      </ListItem>
      {partnerUser ? <ListItem padded><Icon paddedIcon name="one-person"/><Text>{`Assigned to ${partnerUser.firstName} ${partnerUser.lastName}`}</Text></ListItem> : null}
      {requiredDate ? <ListItem padded><Icon paddedIcon name="delivery-time"/><Text>{moment(requiredDate).format('dddd Do MMMM, h:mma')}</Text></ListItem> : null}
      {noPeopleRequired ? <ListItem padded><Icon paddedIcon name="one-person"/><Text key='text'>{`${noPeopleRequired} ${noPeopleRequired > 1 ?   'people' : 'person'} required`}</Text></ListItem> : null}
      {orderProduct ? <ListItem padded>
        {orderProduct.imageUrl ? <Icon paddedIcon name={orderProduct.imageUrl}/> : null}
        <Text>{`${orderProduct.name}`}</Text>
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
  const {order} = initialProps;
  const {orderContentType} = order;
  return {
    orderContentType,
    ...initialProps
  };
};

const ConnectedOrderSummary = connect(
  mapStateToProps
)(OrderSummary);

export {ConnectedOrderSummary as OrderSummary};


