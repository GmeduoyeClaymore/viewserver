import React, {Component} from 'react';
import {View, Text, Row, Grid, Col, ListItem} from 'native-base';
import {Icon} from 'common/components';
import moment from 'moment';
import shotgun from 'native-base-theme/variables/shotgun';

export default class UserRatingsDetail extends Component{
  render(){
    const {user} = this.props;
    const {ratings = []} = user;

    if (ratings.length == 0 || !Array.isArray(ratings)){
      return <Text note style={styles.noJobs}>{`${user.firstName} has no completed jobs yet`}</Text>;
    }

    return ratings.map(rating => {
      return <ListItem key={rating.orderId} padded>
        <Grid>
          <Col size={70}>
            <Text numberOfLines={1} style={{alignSelf: 'flex-start'}}>{rating.title}</Text>

            <View style={{flexDirection: 'row'}}>
              <Icon paddedIcon name="delivery-time"/>
              <Text>{moment(rating.updatedDate).format('Do MMM YYYY')}</Text>
            </View>
          </Col>
          <Col size={30}>
            <Row>
              {[...Array(rating.rating)].map((e, i) => <Icon name='star-full' key={i} style={styles.star}/>)}
            </Row>
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

  star: {
    fontSize: 15,
    color: shotgun.gold
  }
};
