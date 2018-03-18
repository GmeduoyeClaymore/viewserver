import React from 'react';
import { View, Image, TouchableHighlight } from 'react-native';
import {connect} from 'custom-redux';
import { isOperationPending } from 'common/dao';
import {updateRelationship} from 'common/actions/CommonActions';
import {Button, Text} from 'native-base';
import {Icon} from 'common/components';
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
  backgroundColor: 'red'
};

const mapButtonStateToProps = (state, props) => {
  const {dispatch} = props;
  const busy = isOperationPending(state, 'userRelationshipDao', 'updateRelationship');
  const updateRelationshipCmd = (user, relationshipStatus, relationshipType) => {
    dispatch(updateRelationship({targetUserId: user.userId, relationshipStatus, relationshipType}));
  };
  return {
    busy,
    updateRelationshipCmd,
    ...props
  };
};

const ButtonElementFactory  = (tartgetStatus, text) => ({updateRelationshipCmd, user, busy}) => <Button  disabled={busy} onPress={() => updateRelationshipCmd(user, tartgetStatus, 'COLLEAGUE')} style={{marginLeft: 15}}><Text>{text}</Text></Button>;
const StatusButtonElement = (tartgetStatus, text) => connect(mapButtonStateToProps)(ButtonElementFactory(tartgetStatus, text) );
const DisconnectButton = StatusButtonElement('UNKNOWN', 'Disconnect');
const AcceptButton = StatusButtonElement('ACCEPTED', 'Accept');
const IgnoreButton = StatusButtonElement('UNKNOWN', 'Ignore');
const CancelButton = StatusButtonElement('UNKNOWN', 'Cancel Request');
const RequestButton = StatusButtonElement('REQUESTED', 'Request');

const StatusButton = ({user}) => {
  if (user.relationshipStatus === 'ACCEPTED'){
    return <DisconnectButton user={user}/>;
  }
  if (user.relationshipStatus === 'REQUESTED'){
    if (!user.initiatedByMe){
      return <View><AcceptButton user={user}/><IgnoreButton user={user}/></View>;
    }
    return <CancelButton user={user}/>;
  }
  if (!user.relationshipStatus || user.relationshipStatus === 'UNKNOWN'){
    return <RequestButton user={user}/>;
  }
  return 'Unknown ' + user.relationshipStatus;
};


const UserRelationshipItem = ({context, user, selectedUser = {}, onPressAssignUser, onPressCallUser}) => {
  if (!user){
    return null;
  }
  const isSelected = user.userId === selectedUser.userId;

  return <TouchableHighlight style={{flex: 1, flexDirection: 'row', minHeight: 80, backgroundColor: 'white'}} onPress={() => context.setSelectedUser(user) }>
    <View style={isSelected ? containerSelected : styles.container}>
      <Image resizeMode="contain" source={{uri: user.imageUrl}}  style={styles.picture}/>
      <View style={{flex: 1, padding: 5}}>
        <Text style={styles.title}>{user.firstName + ' ' + user.lastName}</Text>
        <Text style={styles.summary}>{'ONLINE: ' + user.online}</Text>
        <Text style={styles.summary}>{'USER STATUS: ' + user.status}</Text>
        <Text style={styles.summary}>{'RELATIONSHIP STATUS: ' + user.relationshipStatus}</Text>
        <Text style={styles.summary}>{'TYPE: ' + user.type}</Text>
        <Text style={styles.summary}>{'STATUS MESSAGE: ' + user.statusMessage}</Text>
        <Text style={styles.summary}>{'RATING: ' + user.rating}</Text>
        <Text style={styles.summary}>{'DISTANCE: ' + user.distance}</Text>
      </View>
      <View style={{flexDirection: 'column'}}>
        {onPressAssignUser ? <Button onPresss={() => {
          onPressAssignUser(user);
        }} style={{marginLeft: 15}}><Text>Assign job</Text></Button> : null}
        <StatusButton user={user}/>
        <Button fullWidth callButton onPress={() => onPressCallUser(user)}>
          <Icon name="phone" paddedIcon/>
          <Text uppercase={false}>Call User</Text>
        </Button>
      </View>
    </View>
  </TouchableHighlight>;
};

export default UserRelationshipItem;


