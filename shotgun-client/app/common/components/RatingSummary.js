import React from 'react';
import {Text, View} from 'native-base';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import shotgun from 'native-base-theme/variables/shotgun';
import {RatingAction, Icon} from 'common/components';
import {Image} from 'react-native';

export const RatingSummary = ({orderSummary, isPartner}) => {
  const {delivery} = orderSummary;
  const name = isPartner ? delivery.customerFirstName : delivery.partnerFirstName;
  const rating = isPartner ? orderSummary.customerRating : orderSummary.partnerRating;

  const isComplete = orderSummary.status == OrderStatuses.COMPLETED;
  const isRated = rating !== 0 && rating !== undefined;

  if (!isComplete) {
    return null;
  }

  if (isRated) {
    const stars = [...Array(rating)].map((e, i) => <Icon name='star' key={i} style={styles.star}/>);
    return <View style={styles.view}>
      {!isPartner ?
        <Image source={{uri: delivery.partnerImageUrl}} resizeMode='contain' style={styles.partnerImage}/> : null}
      <Text style={styles.text}>You rated {name}</Text>{stars}
    </View>;
  }

  return <RatingAction isPartner={isPartner} orderSummary={orderSummary}/>;
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
  partnerImage: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 30,
    marginRight: 10
  }
};
