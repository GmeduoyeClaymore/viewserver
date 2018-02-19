import React from 'react';
import {Text, View} from 'native-base';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import shotgun from 'native-base-theme/variables/shotgun';
import {RatingAction, Icon} from 'common/components';
import {Image} from 'react-native';

export const RatingSummary = ({orderSummary, isDriver}) => {
  const {delivery} = orderSummary;
  const name = isDriver ? delivery.customerFirstName : delivery.driverFirstName;
  const rating = isDriver ? orderSummary.customerRating : orderSummary.driverRating;

  const isComplete = orderSummary.status == OrderStatuses.COMPLETED;
  const isRated = rating !== 0 && rating !== undefined;

  if (!isComplete) {
    return null;
  }

  if (isRated) {
    const stars = [...Array(rating)].map((e, i) => <Icon name='star' key={i} style={styles.star}/>);
    return <View style={styles.view}>
      {!isDriver ?
        <Image source={{uri: delivery.driverImageUrl}} resizeMode='contain' style={styles.driverImage}/> : null}
      <Text style={styles.text}>You rated {name}</Text>{stars}
    </View>;
  }

  return <RatingAction isDriver={isDriver} orderSummary={orderSummary}/>;
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
  driverImage: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 30,
    marginRight: 10
  }
};
