import React, {Component} from 'react';
import {Button, Text, Col, Grid} from 'native-base';
import {Icon, AverageRating} from 'common/components';
import {callPartner} from 'customer/actions/CustomerActions';
import {callCustomer} from 'partner/actions/PartnerActions';
import {Image} from 'react-native';

export class UserInfo extends Component{
  onPressCall = async () => {
    const {dispatch, isPartner, orderId} = this.props;
    dispatch(isPartner ? callCustomer(orderId) : callPartner(orderId));
  };

  render(){
    const {user} = this.props;

    return <Grid>
      <Col>
        <Col>
          <Image source={{uri: user.imageUrl}} resizeMode='contain' style={styles.images}/>
        </Col>
        <Col>
          <Text>{user.firstName} {user.lastName}</Text>
          <AverageRating rating={user.ratingAvg}/>
        </Col>
      </Col>
      <Col>
        <Button fullWidth callButtonSml onPress={this.onPressCall}>
          <Icon name="phone" paddedIcon/>
          <Text uppercase={false}>Call</Text>
        </Button>
      </Col>
    </Grid>;
  }
}

const styles = {
  images: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 40,
    marginRight: 10
  },
};
