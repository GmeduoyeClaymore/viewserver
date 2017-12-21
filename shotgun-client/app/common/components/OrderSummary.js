import React, {Component} from 'react';
import {Dimensions, Image} from 'react-native';
import {Text, List, ListItem, H3} from 'native-base';
import MapViewStatic from './maps/MapViewStatic';

export default class OrderSummary extends Component{
  constructor(){
    super();
  }

  render() {
    const { width } = Dimensions.get('window');
    const {orderItem, delivery, client} = this.props;
    const {origin, destination} = delivery;

    return <List>
      <ListItem>
        <Text>Your guide price is</Text>
        <H3>£XX.XX</H3>
      </ListItem>

      <MapViewStatic client={client} width={width} height={100} origin={origin} destination={destination}/>

      <ListItem>
        <Text>{origin.line1}, {origin.postCode}</Text>
      </ListItem>

      {destination.line1 !== undefined ? <ListItem>
        <Text>{destination.line1}, {destination.postCode}</Text>
      </ListItem> : null}

      <ListItem>
        <H3>Help required?</H3>
        <Text>{delivery.noRequiredForOffload == 0 ? 'No' : `Yes: ${delivery.noRequiredForOffload } People to help`}</Text>
      </ListItem>

      <ListItem>
        <H3>Item Details</H3>
        <Text>{orderItem.notes}</Text>
        {orderItem.imageData !== undefined ?  <Image source={{uri: `data:image/jpeg;base64,${orderItem.imageData}`}} style={styles.image}/> : null}
      </ListItem>
    </List>;
  }
}

const styles = {
  image: {
    flex: 1,
    height: 200
  }
};

