import React from 'react';
import {View, Text} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import {Icon} from 'common/components';

export default UserMarker = ({user, productId}) => {
  //TODO - second ternary shouldn't be needed it's just because the test data sometiems doesn't have selected content types
  const icon = productId ? productId : (user.selectedContentTypes && typeof user.selectedContentTypes == 'object'  ? `content-type-${Object.keys(user.selectedContentTypes)[0]}` : 'one-person');

  return [
    <View key='bubble' mapBubble style={styles.bubble}>
      <Icon name={icon} style={styles.icon}/>
      <Text numberOfLines={1} style={styles.text}>{`${user.firstName} ${user.lastName}`}</Text>
    </View>,
    <View key='arrow' mapArrow style={styles.arrow}/>];
};

const styles = {
  bubble: {
    borderColor: shotgun.brandDark,
    width: 100,
    flexDirection: 'row',
    overflow: 'hidden'
  },
  text: {
    width: 65,
    fontSize: 10
  },
  arrow: {
    borderTopColor: shotgun.brandDark
  },
  icon: {
    fontSize: 16,
    marginRight: 5
  }
};
