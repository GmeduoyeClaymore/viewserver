import React, {Component} from 'react';
import {View, Text, Row, Grid, Col, ListItem} from 'native-base';
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
        <Grid>
          <Col size={70}  onPress={showRatingImages}>
            <View style={styles.comment}>
              {rating.images ? <Icon paddedIcon name="camera"/> : null}
              <Text numberOfLines={1} style={styles.title}>{rating.title}</Text>
            </View>
            <View style={styles.time}>
              <Icon paddedIcon name="delivery-time"/>
              <Text>{moment(rating.updatedDate).format('Do MMM YYYY')}</Text>
            </View>
            {rating.comments ? <Text style={styles.comments}>{rating.comments}</Text> : null}
          </Col>
          <Col size={30}>
            <Row style={styles.starRow}>
              {[...Array(rating.rating)].map((e, i) => <Icon name='star-full' key={i} style={styles.star}/>)}
            </Row>
          </Col>
          <Col size={1}>
            {rating.images ? <RatingImages ref={ref => {pictureControl = ref;}} images={rating.images}/> : undefined}
          </Col>
        </Grid>
 
      </ListItem>;
    });
  }
}

const styles = {
  noJobs: {
    margin: shotgun.contentPadding
  },
  starRow: {
    justifyContent: 'flex-end'
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
  }
};
