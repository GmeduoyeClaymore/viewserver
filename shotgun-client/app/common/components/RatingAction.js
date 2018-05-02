import React from 'react';
import {Text, View} from 'native-base';
import {connect} from 'custom-redux';
import shotgun from 'native-base-theme/variables/shotgun';
import {rateCustomer} from 'partner/actions/PartnerActions';
import {ratePartner} from 'customer/actions/CustomerActions';
import {Icon} from 'common/components';

const RatingAction = ({isPartner, order, dispatch}) => {
  const {assignedPartner, customer} = order;
  const name = isPartner ? assignedPartner.firstName : customer.firstName;
  const rating = isPartner ? assignedPartner.ratingAvg : customer.ratingAvg;

  const onPressStar = (newRating) => {
    const ratingFunc = isPartner ? rateCustomer : ratePartner;
    const action = ratingFunc(order.orderId, newRating);
    dispatch(action);
  };

  return <View style={{alignItems: 'center'}}><Text style={{alignItems: 'center'}}>Rate {name}</Text>
    <View style={styles.starView}>
      <Icon name='star' onPress={() => onPressStar(1)}
        style={[styles.star, rating > 0 ? styles.starFilled : styles.starEmpty]}/>
      <Icon name='star' onPress={() => onPressStar(2)}
        style={[styles.star, rating > 1 ? styles.starFilled : styles.starEmpty]}/>
      <Icon name='star' onPress={() => onPressStar(3)}
        style={[styles.star, rating > 2 ? styles.starFilled : styles.starEmpty]}/>
      <Icon name='star' onPress={() => onPressStar(4)}
        style={[styles.star, rating > 3 ? styles.starFilled : styles.starEmpty]}/>
      <Icon name='star' onPress={() => onPressStar(5)}
        style={[styles.star, rating > 4 ? styles.starFilled : styles.starEmpty]}/>
    </View>
  </View>;
};

const styles = {
  starView: {
    flexWrap: 'wrap',
    alignItems: 'center',
    flexDirection: 'row'
  },
  star: {
    fontSize: 35,
    padding: 2
  },
  starFilled: {
    color: shotgun.gold,
  },
  starEmpty: {
    color: shotgun.brandLight
  }
};

const ConnectedRatingAction = connect(undefined, true, false)(RatingAction);
export {ConnectedRatingAction as RatingAction};

