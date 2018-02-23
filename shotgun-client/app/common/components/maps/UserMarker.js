import React from 'react';
import {View, Text} from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';

export default UserMarker = ({user}) => {
  return (
    <View style={styles.container}>
      <View style={styles.bubble}>
        <Text numberOfLines={1} style={styles.product}>{user.firstName + ' ' + user.lastName}</Text>
      </View>
      <View style={styles.arrowBorder} />
      <View style={styles.arrow} />
    </View>
  );
};

const styles = {
  container: {
    flexDirection: 'column',
    alignSelf: 'flex-start'
  },
  bubble: {
    flex: 0,
    flexDirection: 'row',
    alignSelf: 'flex-start',
    backgroundColor: shotgun.brandSecondary,
    padding: 2,
    borderRadius: 3,
    borderColor: shotgun.brandSecondary,
    borderWidth: 0.5,
    width: 75
  },
  product: {
    color: shotgun.brandDark,
    fontSize: 10,
    fontWeight: 'bold'
  },
  arrow: {
    backgroundColor: 'transparent',
    borderWidth: 4,
    borderColor: 'transparent',
    borderTopColor: shotgun.brandSecondary,
    alignSelf: 'center',
    marginTop: -9,
  },
  arrowBorder: {
    backgroundColor: 'transparent',
    borderWidth: 4,
    borderColor: 'transparent',
    borderTopColor: shotgun.brandSecondary,
    alignSelf: 'center',
    marginTop: -0.5,
  },
};
