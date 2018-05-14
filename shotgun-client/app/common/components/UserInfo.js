import React, {Component} from 'react';
import {Button, Text, Grid, Col, Row} from 'native-base';
import {Icon, AverageRating} from 'common/components';
import {Image} from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';
import {callUser} from 'common/actions/CommonActions';

export class UserInfo extends Component{
  onPressCall = async () => {
    const {dispatch, user} = this.props;
    dispatch(callUser(user.userId));
  };

  render(){
    const {user, imageWidth = 40, showCallButton = true} = this.props;
    return user ? <Grid>
      <Row>
        {user.imageUrl ? <Image source={{uri: user.imageUrl}} resizeMode='contain' style={[styles.images, {width: imageWidth}]}/> : null}
        <Col style={styles.name}>
          <Text>{user.firstName} {user.lastName}</Text>
          <AverageRating rating={user.ratingAvg}/>
        </Col>
        {showCallButton ? <Col>
          <Button fullWidth callButton onPress={this.onPressCall}>
            <Icon name="phone" paddedIcon/>
            <Text uppercase={false}>Call</Text>
          </Button>
        </Col> : null}
      </Row>
    </Grid> : null;
  }
}

const styles = {
  name: {
    alignContent: 'flex-start'
  },
  images: {
    aspectRatio: 1,
    borderRadius: shotgun.imageBorderRadius,
    marginRight: 10
  },
};
