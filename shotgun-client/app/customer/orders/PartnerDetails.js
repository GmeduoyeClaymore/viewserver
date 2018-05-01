import React, {Component} from 'react';
import {Button, Text, Col, Row} from 'native-base';
import {ErrorRegion, Icon, AverageRating} from 'common/components';
import {callPartner} from 'customer/actions/CustomerActions';
import {Image} from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';
import * as ContentTypes from 'common/constants/ContentTypes';
/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('DetailsTitle', ({product}) => `Your ${product.name}`).
    delivery(() => 'Your delivery Driver').
    rubbish(() => 'Your Rubbish collector').
  property('CallButtonTitle', ({product}) => `Call ${product.name}`).
  delivery(() => 'Call Delivery Partner').
    rubbish(() => 'Call Rubbish collector');
/*eslint-enable */


export default class PartnerDetails extends Component{
  constructor(props){
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  render(){
    const {orderSummary, dispatch} = this.props;
    const {product, delivery} = orderSummary;
    const {resources} = this;
    const {DetailsTitle, CallButtonTitle} = resources;


    const onPressCallPartner = async () => {
      dispatch(callPartner(orderSummary.orderId));
    };

    return [<Row>
      <Col>
        <Text key="title" style={styles.subTitle}><DetailsTitle product={product}/></Text>
        <Text key="summary" style={styles.data}>{delivery.partnerFirstName} {delivery.partnerLastName}</Text>
        <AverageRating rating={delivery.partnerRatingAvg}/>
      </Col>
      <Col>
        <Image source={{uri: delivery.partnerImageUrl}} resizeMode='contain' style={styles.partnerImage}/>
      </Col>
    </Row>,
    <Row>
      <Button fullWidth callButtonSml onPress={onPressCallPartner}>
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
  partnerImage: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 40,
    marginRight: 10
  },
};
  
