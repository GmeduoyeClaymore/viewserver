import React, {Component} from 'react';
import {Button, Text, Col, Row} from 'native-base';
import {ErrorRegion, Icon, AverageRating} from 'common/components';
import {callDriver} from 'customer/actions/CustomerActions';
import {Image} from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';
import * as ContentTypes from 'common/constants/ContentTypes';
/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('DetailsTitle', ({product}) => `Your ${product.name}`).
    delivery(() => 'Your delivery driver').
    rubbish(() => 'Your Rubbish collector').
  property('CallButtonTitle', ({product}) => `Call ${product.name}`).
  delivery(() => 'Call Delivery Driver').
    rubbish(() => 'Call Rubbish collector');
/*eslint-enable */


export default class DriverDetails extends Component{
  constructor(props){
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  render(){
    const {orderSummary, dispatch} = this.props;
    const {product, delivery} = orderSummary;
    const {resources} = this;
    const {DetailsTitle, CallButtonTitle} = resources;


    const onPressCallDriver = async () => {
      dispatch(callDriver(orderSummary.orderId));
    };

    return [<Row>
      <Col>
        <Text key="title" style={styles.subTitle}><DetailsTitle product={product}/></Text>
        <Text key="summary" style={styles.data}>{delivery.driverFirstName} {delivery.driverLastName}</Text>
        <AverageRating rating={delivery.driverRatingAvg}/>
      </Col>
      <Col>
        <Image source={{uri: delivery.driverImageUrl}} resizeMode='contain' style={styles.driverImage}/>
      </Col>
    </Row>,
    <Row>
      <Button fullWidth callButtonSml onPress={onPressCallDriver}>
        <Icon name="phone" paddedIcon/>
        <Text uppercase={false}><CallButtonTitle product={product}/></Text>
      </Button>
    </Row>];
  }
}


const styles = {
  subTitle: {
    color: shotgun.brandLight,
    fontSize: 12,
    borderWidth: 10,
    borderColor: 'red',
    whitespace: 'nowrap',
    paddingBottom: 5
  },
  driverImage: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 40,
    marginRight: 10
  },
};
  
