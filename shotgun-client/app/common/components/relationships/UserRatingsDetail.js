import React, {Component} from 'react';
import {View, Text, ListItem, Button} from 'native-base';
import {Icon} from 'common/components';
import moment from 'moment';
import shotgun from 'native-base-theme/variables/shotgun';
import RatingImages from './RatingImages';
export default class UserRatingsDetail extends Component{
  render(){
    const {user} = this.props;
    const {ratings = []} = user;

    if (ratings.length == 0 || !Array.isArray(ratings)){
      return <Text note style={styles.noJobs}>{`${user.firstName} has no completed jobs yet`}</Text>;
    }

    return ratings.map(rating => {
      let pictureControl = undefined;
      const showRatingImages = () => {
        if (pictureControl){
          pictureControl.showLightBox();
        }
      };
      return <ListItem key={rating.orderId} padded>
        <View style={{flexDirection: 'column', width: '70%'}}>
          <Text numberOfLines={1} style={styles.title}>{rating.title}</Text>
          <View style={styles.time}>
            <Icon paddedIcon name="delivery-time"/>
            <Text>{moment(rating.updatedDate).format('Do MMM YYYY')}</Text>
          </View>
          {rating.comments ? <Text style={styles.comments}>{rating.comments}</Text> : null}
        </View>
        <View style={{flexDirection: 'column', width: '30%'}}>
          <View style={styles.starRow}>
            {[...Array(rating.rating)].map((e, i) => <Icon name='star-full' key={i} style={styles.star}/>)}
          </View>
          {rating.images ? <Button light fullWidth style={styles.cameraButton} onPress={showRatingImages}><Icon style={styles.camera} name="camera"/></Button> : null}
          {rating.images ? <RatingImages ref={ref => {pictureControl = ref;}} images={rating.images}/> : undefined}
        </View>
      </ListItem>;
    });
  }
}

const styles = {
  noJobs: {
    margin: shotgun.contentPadding
  },
  starRow: {
    justifyContent: 'flex-end',
    flexDirection: 'row'
  },
  star: {
    fontSize: 15,
    color: shotgun.gold
  },
  title: {
    alignSelf: 'flex-start'
  },
  time: {
    flexDirection: 'row',
    paddingTop: 5
  },
  comment: {
    flexDirection: 'row',
  },
  comments: {
    alignSelf: 'flex-start',
    color: shotgun.brandLight,
    fontStyle: 'italic',
    paddingTop: 10
  },
  cameraButton: {
    marginTop: 10,
    height: 35
  },
  camera: {
    fontSize: 15,
    margin: 0,
    padding: 0
  }
};
