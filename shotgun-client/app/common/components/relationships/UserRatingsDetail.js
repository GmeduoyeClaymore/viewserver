import React, {Component} from 'react';
import {Text, Row, Grid, Col, ListItem} from 'native-base';
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
      return <ListItem key={rating.orderId} paddedTopBottomNarrow>
        <Grid>
          <Col size={80}>
            <Row>
              <Text style={{marginBottom: 8}}>{rating.title}</Text>
            </Row>
            <Row style={{paddingRight: 10, marginBottom: 8}}>
              <Icon paddedIcon name="delivery-time"/>
              <Text>{moment(rating.updatedDate).format('Do MMM YYYY')}</Text>
            </Row>
          </Col>
          <Col size={20}>
            <Icon name='star' style={styles.star}/>
            <Text numberOfLines={1} note style={styles.averageText}>{rating.rating.toFixed(0)}</Text>
          </Col>
        </Grid>
      </ListItem>;
    });
  }
}

const styles = {
  noJobs: {
    marginTop: shotgun.contentPadding
  },
  star: {
    fontSize: 40,
    color: shotgun.gold,
    position: 'absolute'
  },
  averageText: {
    position: 'absolute',
    right: 59,
    top: 13,
    zIndex: 2
  }
};
