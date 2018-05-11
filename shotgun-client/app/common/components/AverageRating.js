import {Icon} from 'common/components';
import {Text, View} from 'native-base';
import React from 'react';

export const AverageRating = ({rating, text = 'No Ratings Yet', decimalPlaces = 1}) => {
  return rating > 0 ?
    <View style={styles.averageView}>
      <Icon name='star' avgStar/>
      <Text numberOfLines={1} note style={styles.averageText}>{rating.toFixed(decimalPlaces)}</Text>
    </View> :
    <Text note>{text}</Text>;
};

const styles = {
  averageView: {
    flexWrap: 'nowrap',
    flexDirection: 'row',
  },
  averageText: {
    alignSelf: 'center'
  }
};
