import React, {Component} from 'react';
import {Button, Text, Col, Row} from 'native-base';
import {Icon, AverageRating} from 'common/components';
import {callPartner} from 'customer/actions/CustomerActions';
import {callCustomer} from 'partner/actions/PartnerActions';
import {Image} from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';

export class UserInfo extends Component{
  onPressCall = async () => {
    const {dispatch, isPartner, orderId} = this.props;
    dispatch(isPartner ? callCustomer(orderId) : callPartner(orderId));
  };

  render(){
    const {user} = this.props;

    return user ? <Row>
      <Image source={{uri: user.imageUrl}} resizeMode='contain' style={styles.images}/>
      <Col style={{flex: 1, alignContent: 'flex-start'}}>
        <Text style={{textAlign: 'left'}}>{user.firstName} {user.lastName}</Text>
        <AverageRating style={{textAlign: 'left'}} rating={user.ratingAvg}/>
      </Col>
      <Button  style={{flex: 1}} fullWidth callButtonSml onPress={this.onPressCall}>
        <Icon name="phone" paddedIcon/>
        <Text uppercase={false}>Call</Text>
      </Button>
    </Row> : null;
  }
}

const styles = {
  images: {
    aspectRatio: 1,
    borderRadius: shotgun.imageBorderRadius,
    width: 40,
    marginRight: 10
  },
};
