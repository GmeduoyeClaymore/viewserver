import {Icon} from 'common/components';
import {Text} from 'native-base';
import React from 'react';
import StarRating from 'common/stars/StarRating';

export const AverageRating = ({rating = 0}) => {
  <StarRating
    disabled={true}
    maxStars={5}
    rating={rating}
    reversed
    starSize={50}
  />;
};

const styles = {
  averageText: {
    fontSize: 10,
    minWidth: 25
  }
};
