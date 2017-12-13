import React from 'react';
import {View, Text} from 'react-native';

export default AddressMarker = ({address}) => {
  return (
    <View style={styles.container}>
      <View style={styles.bubble}>
        <Text numberOfLines={1} style={styles.amount}>{address}</Text>
      </View>
      <View style={styles.arrowBorder} />
      <View style={styles.arrow} />
    </View>
  );
};

const styles = {
  container: {
    flexDirection: 'column',
    alignSelf: 'flex-start',
  },
  bubble: {
    flex: 0,
    flexDirection: 'row',
    alignSelf: 'flex-start',
    backgroundColor: '#FF5A5F',
    padding: 2,
    borderRadius: 3,
    borderColor: '#D23F44',
    borderWidth: 0.5,
    width: 75
  },
  amount: {
    color: '#FFFFFF',
    fontSize: 10
  },
  arrow: {
    backgroundColor: 'transparent',
    borderWidth: 4,
    borderColor: 'transparent',
    borderTopColor: '#FF5A5F',
    alignSelf: 'center',
    marginTop: -9,
  },
  arrowBorder: {
    backgroundColor: 'transparent',
    borderWidth: 4,
    borderColor: 'transparent',
    borderTopColor: '#D23F44',
    alignSelf: 'center',
    marginTop: -0.5,
  },
};
