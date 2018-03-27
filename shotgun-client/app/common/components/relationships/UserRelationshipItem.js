import React from 'react';
import { View, TouchableHighlight } from 'react-native';
import {RelatedUser} from './RelatedUser';
import shotgun from 'native-base-theme/variables/shotgun';

const UserRelationshipItem = ({setSelectedUser, user, selectedUser = {}, onPressAssignUser, onPressCallUser}) => {
  if (!user){
    return null;
  }
  const isSelected = user.userId === selectedUser.userId;

  return <TouchableHighlight style={styles.button} onPress={() => setSelectedUser(user) }>
    <View style={isSelected ? containerSelected : styles.view}>
      <RelatedUser {...{user, selectedUser, onPressAssignUser, onPressCallUser}}/>
    </View>
  </TouchableHighlight>;
};

const styles = {
  button: {
    flex: 1,
    flexDirection: 'row',
    minHeight: 80,
    backgroundColor: shotgun.brandPrimary
  },
  view: {
    flex: 1,
    flexDirection: 'row',
    padding: 5,
    paddingTop: 10
  }
};

const containerSelected = {
  ...styles.view,
  backgroundColor: shotgun.brandPrimary
};

export default UserRelationshipItem;


