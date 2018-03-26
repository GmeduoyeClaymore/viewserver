import {Icon} from 'common/components';
import {Text} from 'native-base';
import React from 'react';

export const AverageRating = ({rating, text = 'No Ratings Yet'}) => {
  return rating > 0 ? [<Icon name='star' key='star' avgStar/>, <Text key='text' style={styles.averageText}>{rating.toFixed(1)}</Text>] : <Text style={styles.averageText}>{text}</Text>;
};

const styles = {
  averageText: {
    fontSize: 10,
    minWidth: 25
  }
};
