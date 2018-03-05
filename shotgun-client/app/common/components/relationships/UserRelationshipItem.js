import React from 'react';
import { View, Text, Image, TouchableHighlight, StyleSheet } from 'react-native';

const styles = {
  container: {
    flex: 1,
    flexDirection: 'row',
    padding: 5,
    paddingTop: 10
  },
  picture: {
    width: 60,
    height: 60,
  },
  title: {
    fontWeight: 'bold',
    color: '#848484',
    fontSize: 12
  },
  summary: {
    fontSize: 10
  }
};

const containerSelected = {
  ...styles.container,
  backgroundColor: 'red'
};

const UserRelationshipItem = ({context, user, selectedUser = {}}) => {
  if (!user){
    return null;
  }
  const isSelected = user.userId === selectedUser.userId;

  return <TouchableHighlight style={{flex: 1, flexDirection: 'row', minHeight: 80, backgroundColor: 'white'}} onPress={() => context.setSelectedUser(user) }>
    <View style={isSelected ? styles.container : containerSelected}>
      <Image resizeMode="contain" source={{uri: user.imageUrl}}  style={styles.picture}/>
      <View style={{flex: 1, padding: 5}}>
        <Text style={styles.title}>{user.firstName + ' ' + user.lastName}</Text>
        <Text style={styles.summary}>{'ONLINE: ' + user.online}</Text>
        <Text style={styles.summary}>{'STATUS: ' + user.status}</Text>
        <Text style={styles.summary}>{'TYPE: ' + user.type}</Text>
        <Text style={styles.summary}>{'STATUS MESSAGE: ' + user.statusMessage}</Text>
        <Text style={styles.summary}>{'DISTANCE: ' + user.distance}</Text>
      </View>
    </View>
  </TouchableHighlight>;
};

export default UserRelationshipItem;


