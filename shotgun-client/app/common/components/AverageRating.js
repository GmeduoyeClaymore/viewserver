import {Icon} from 'common/components';
import {Text, View} from 'native-base';
import React from 'react';

export const AverageRating = ({rating, text = 'No Ratings Yet'}) => {
  return rating > 0 ?
    <View style={{flex: 1, flexWrap: 'nowrap', flexDirection: 'row'}}><Icon name='star' key='star' avgStar/><Text numberOfLines={1} key='text' style={styles.averageText}>{rating.toFixed(1)}</Text></View> :
    <Text style={styles.averageText}>{text}</Text>;
};

const styles = {
  averageText: {
    fontSize: 10
  }
};
