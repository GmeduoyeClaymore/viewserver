import React from 'react';
import {View, Image} from 'react-native';
import {Button, Text} from 'native-base';
import {Icon, ErrorRegion, AverageRating} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';
import {connect} from 'custom-redux';
import { isOperationPending } from 'common/dao';
import {updateRelationship} from 'common/actions/CommonActions';

const styles = {
  view: {
    alignItems: 'flex-start',
    justifyContent: 'flex-start',
    flexDirection: 'row'
  },
  starFilled: {
    fontSize: 15,
    padding: 2,
    color: shotgun.gold,
  },
  starEmpty: {
    fontSize: 15,
    padding: 2,
    color: shotgun.brandLight
  },
  picture: {
    borderRadius: 40,
    width: 80,
    height: 80,
    marginRight: 10
  },
  title: {
    fontWeight: 'bold',
    color: '#848484',
    fontSize: 18
  },
  summary: {
    fontSize: 10,
    paddingTop: 5,
    paddingBottom: 5
  },
};

const getStatusColor = (status) => {
  if (status === 'ONLINE'){
    return 'green';
  }
  if (status === 'AVAILABLESOON'){
    return 'orange';
  }
  if (status === 'BUSY'){
    return 'blue';
  }
  return 'red';
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

const ButtonElementFactory  = (tartgetStatus, text) => ({updateRelationshipCmd, user, busy, style}) =>
  <Button  style={{...style, justifyContent: 'flex-start'}} statusButton fullWidth disabled={busy} onPress={() => updateRelationshipCmd(user, tartgetStatus, 'COLLEAGUE')}>
    <Icon name="dashed" paddedIcon/>
    <Text>{text}</Text>
  </Button>;
const StatusButtonElement = (tartgetStatus, text) => connect(mapButtonStateToProps)(ButtonElementFactory(tartgetStatus, text) );
const DisconnectButton = StatusButtonElement('UNKNOWN', 'Un-Friend');
const AcceptButton = StatusButtonElement('ACCEPTED', 'Accept Request');
const IgnoreButton = StatusButtonElement('UNKNOWN', 'Ignore Request');
const CancelButton = StatusButtonElement('UNKNOWN', 'Cancel Request');
const RequestButton = StatusButtonElement('REQUESTED', 'Add as friend');

export const StatusButton = ({user, style}) => {
  if (user.relationshipStatus === 'ACCEPTED'){
    return <DisconnectButton user={user} style={style}/>;
  }
  if (user.relationshipStatus === 'REQUESTED'){
    if (!user.initiatedByMe){
      return <View  style={style}><AcceptButton user={user}  style={style}/><IgnoreButton  style={style} user={user}/></View>;
    }
    return <CancelButton  style={style} user={user}/>;
  }
  if (!user.relationshipStatus || user.relationshipStatus === 'UNKNOWN'){
    return <RequestButton  style={style} user={user}/>;
  }
  return 'Unknown ' + user.relationshipStatus;
};

export const RelatedUser = ({user, onPressCallUser, errors, style = {}}) => {
  return <View style={{...style, flex: 1}}>

    <View style={{flexDirection: 'row', minHeight: 80, flex: 10}}>
      <Image resizeMode="contain" source={{url: user.imageUrl}}  style={{...styles.picture, borderColor: getStatusColor(user.status), borderWidth: 2}}/>
      <View style={{flex: 3, padding: 5}}>
        <View style={{flexDirection: 'column'}}>
          <Text style={{...styles.title, marginLeft: 1}}>{user.firstName + ' ' + user.lastName}</Text>
          {user.status ? <Text style={styles.summary}>{' (' + user.status + ')'}</Text> : null}
        </View>
        {user.statusMessage ? <Text style={{...styles.summary, marginLeft: 3}}>{user.statusMessage}</Text> : null}
        <Text style={{...styles.summary, marginLeft: 3}}>{ `${Math.round(user.distance)}km away`}</Text>
        <AverageRating rating={user.ratingAvg}/>
      </View>
      <Button style={{marginBottom: 10, marginLeft: 6, marginTop: 10, justifyContent: 'flex-start'}}  fullWidth statusButtonSml onPress={() => onPressCallUser(user)}>
        <Icon name="phone" paddedIcon style={{marginLeft: 22, marginTop: 0}}/>
      </Button>
    </View>
    <ErrorRegion errors={errors}/>
  </View>;
};
