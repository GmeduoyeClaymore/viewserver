import React from 'react';
import {Text, View} from 'native-base';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import shotgun from 'native-base-theme/variables/shotgun';
import {RatingAction, Icon} from 'common/components';
import {Image} from 'react-native';

export const RatingSummary = ({order, isRatingCustomer}) => {
  const {assignedPartner, customer} = order;
  if (!assignedPartner){
    return null;
  }
  const name = isRatingCustomer ?  customer.firstName : assignedPartner.firstName;
  const rating = isRatingCustomer ?  order.ratingCustomer : order.ratingPartner;
  const imageUrl = isRatingCustomer ?  customer.imageUrl : assignedPartner.imageUrl;
  const isComplete = order.orderStatus == OrderStatuses.COMPLETED;
  const isRated = !!rating;

  if (!isComplete) {
    return null;
  }

  if (isRated) {
    const stars = [...Array(rating.rating)].map((e, i) => <Icon name='star-full' key={i} style={styles.star}/>);
    return <View style={styles.view}>
      {imageUrl ?
        <Image source={{uri: imageUrl}} resizeMode='contain' style={styles.images}/> : null}
      <Text style={styles.text}>You rated {name}</Text>{stars}
    </View>;
  }

  return <RatingAction isRatingCustomer={isRatingCustomer} order={order}/>;
};

const styles = {
  view: {
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    marginTop: 5
  },
  text: {
    marginRight: 5
  },
  star: {
    fontSize: 15,
    padding: 2,
    color: shotgun.gold,
  },
  images: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 30,
    marginRight: 10
  }
};
