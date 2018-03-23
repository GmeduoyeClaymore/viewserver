import React from 'react';
import { View, TouchableHighlight } from 'react-native';
import {RelatedUser} from './RelatedUser';
import shotgun from 'native-base-theme/variables/shotgun';
const styles = {
  container: {
    flex: 1,
    flexDirection: 'row',
    padding: 5,
    paddingTop: 10
  },
  picture: {
    marginTop: 15,
    marginRight: 15,
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
  backgroundColor: shotgun.brandPrimary
};


const UserRelationshipItem = ({context, user, selectedUser = {}, onPressAssignUser, onPressCallUser}) => {
  if (!user){
    return null;
  }
  const isSelected = user.userId === selectedUser.userId;

  return <TouchableHighlight style={{flex: 1, flexDirection: 'row', minHeight: 80, backgroundColor: 'white'}} onPress={() => context.setSelectedUser(user) }>
    <View style={isSelected ? containerSelected : styles.container}>
      <RelatedUser {...{context, user, selectedUser, onPressAssignUser, onPressCallUser}}/>
    </View>
  </TouchableHighlight>;
};

export default UserRelationshipItem;


