import {Icon} from 'common/components';
import {Text, View} from 'native-base';
import React from 'react';
import Logger from 'common/Logger';

const formatRating = (rating, decimals) => {
  if (!rating){
    return rating;
  }
  try {
    if (typeof rating === 'string'){
      return parseFloat(rating).toFixed(decimals);
    }
    if (rating.toFixed ){
      return rating.toFixed(decimals);
    }
    return parseFloat(rating + '').toFixed(decimals);
  } catch (error){
    Logger.error(error);
    return rating;
  }
};

export const AverageRating = ({rating, text = 'No Ratings Yet', decimalPlaces = 1}) => {
  return rating > 0 ?
    <View style={styles.averageView}>
      <Icon name='star-full' avgStar style={{alignSelf: 'center'}}/>
      <Text numberOfLines={1} note style={styles.averageText}>{formatRating(rating, decimalPlaces)}</Text>
    </View> :
    <Text note>{text}</Text>;
};

const styles = {
  averageView: {
    flexWrap: 'nowrap',
    flexDirection: 'row',
    alignSelf: 'center'
  },
  averageText: {
    alignSelf: 'center'
  }
};
