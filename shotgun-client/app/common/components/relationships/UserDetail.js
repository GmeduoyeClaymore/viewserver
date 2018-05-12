import React, {Component} from 'react';
import {View, ScrollView} from 'react-native';
import ReactNativeModal from 'react-native-modal';
import {UserInfo, Tabs} from 'common/components';
import UserContentTypeDetail from './UserContentTypeDetail';
import UserRatingsDetail from './UserRatingsDetail';
import {Tab, Text, Row, Grid, Button} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import {updateRelationship} from 'common/actions/CommonActions';
import {withExternalState, ReduxRouter, Route} from 'custom-redux';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getDao} from 'common/dao';

class UserDetail extends Component{
  handleCancel = () => {
    const {history, userRelationshipBasePath} = this.props;
    history.push({pathname: userRelationshipBasePath, transition: 'immediate'});
  }

  beforeNavigateTo(){
    this.subscribeToUser(this.props);
  }

  componentWillReceiveProps(newProps){
    if (newProps.userId != this.props.userId){
      this.subscribeToUser(newProps);
    }
  }

  subscribeToUser = (props) => {
    const {dispatch, userId, selectedUser} = props;
    if (!selectedUser) {
      dispatch(resetSubscriptionAction('singleUserDao', {userId}));
    }
  }

  onUpdateRelationship = (relationshipStatus, relationshipType) => {
    const {dispatch, selectedUser} = this.props;

    dispatch(updateRelationship({targetUserId: selectedUser.userId, relationshipStatus, relationshipType}));
  }

  getActionButton = (relationshipStatus, label, additionalProps) => {
    const {busy} = this.props;

    return <Button fullWidth style={styles.statusButton} {...additionalProps} disabled={busy} onPress={() => this.onUpdateRelationship(relationshipStatus, 'COLLEAGUE')}>
      <Text uppercase={false}>{label}</Text>
    </Button>;
  }

  getStatusButton = () => {
    const {selectedUser} = this.props;
    const buttonsToRender = [];
    if (selectedUser.relationshipStatus != 'BLOCKED'){
      buttonsToRender.push(this.getActionButton('BLOCKED', 'Block User', {danger: true}));
    }
    if (selectedUser.relationshipStatus === 'BLOCKED'){
      buttonsToRender.push(this.getActionButton('UNKNOWN', 'UnBlock User'));
    } else if (selectedUser.relationshipStatus === 'ACCEPTED'){
      buttonsToRender.push(this.getActionButton('UNKNOWN', 'Un-Friend'));
    } else if (selectedUser.relationshipStatus === 'REQUESTED'){
      buttonsToRender.push(this.getActionButton('ACCEPTED', 'Accept Request'));
      buttonsToRender.push(this.getActionButton('UNKNOWN', 'Ignore Request'));
    } else if (selectedUser.relationshipStatus === 'REQUESTEDBYME'){
      buttonsToRender.push(this.getActionButton('UNKNOWN', 'Cancel Request'));
    } else if (!selectedUser.relationshipStatus || selectedUser.relationshipStatus === 'UNKNOWN'){
      buttonsToRender.push(this.getActionButton('REQUESTED', 'Add as friend'));
    } else {
      return 'Unknown ' + selectedUser.relationshipStatus;
    }
    return buttonsToRender;
  }

  goToTabNamed = (name) => {
    const {history, path} = this.props;
    history.replace({pathname: `${path}/${name}`});
  }

  render(){
    const {path, history, dispatch, onPressAssignUser, handleCancel, selectedUser} = this.props;
    const page = history.location.pathname.endsWith('Skills')  ? 1 : 0;

    return selectedUser ? <ReactNativeModal isVisible={history.location.pathname.includes(path)} style={styles.modal}>
      <Grid>
        {onPressAssignUser ? <Button fullWidth style={styles.actionButton}  onPress={() => {onPressAssignUser(selectedUser); handleCancel();}}>
          <Text uppercase={false}>Assign Job To User</Text>
        </Button> : null
        }
        <Row style={styles.userInfoView}>
          <UserInfo dispatch={dispatch} user={selectedUser} imageWidth={60}/>
        </Row>
        <Text style={styles.infoText}>{ `${Math.round(selectedUser.distance)} km away`}</Text>

        <Tabs initialPage={page} page={page}  {...shotgun.tabsStyle}>
          <Tab heading="Completed Jobs" onPress={() => this.goToTabNamed('Ratings')}/>
          <Tab heading="Skills" onPress={() => this.goToTabNamed('Skills')}/>
        </Tabs>

        <ScrollView key='scrollView' style={{flex: 1}}>
          <ReduxRouter {...this.props} path={path}defaultRoute="Ratings">
            <Route key="Ratings" path="Ratings" component={UserRatingsDetail} user={selectedUser}/>
            <Route key="Skills" path="Skills" component={UserContentTypeDetail} selectedContentTypes={selectedUser.selectedContentTypes}/>
          </ReduxRouter>
        </ScrollView>
      </Grid>
      {this.getStatusButton()}
      <Button fullWidth cancelButton onPress={this.handleCancel}>
        <Text uppercase={false}>Close</Text>
      </Button>
    </ReactNativeModal> : null;
  }
}

const mapStateToProps = (state, initialProps) => {
  const userId = getNavigationProps(initialProps).userId;
  if (userId === null){
    throw new Error('Must specify an user id to navigate to this page');
  }
  const daoState = getDao(state, 'singleUserDao');
  if (!daoState){
    return null;
  }
  const selectedUser = getDaoState(state, ['user'], 'singleUserDao');
  const isPendingUserRelationshipSubscription = isAnyOperationPending(state, [{ singleUserDao: 'resetSubscription'}]);
  return {
    ...initialProps,
    userId,
    selectedUser,
    isPendingUserRelationshipSubscription,
    busy: isPendingUserRelationshipSubscription,
  };
};

const styles = {
  modal: {
    margin: 0,
    backgroundColor: shotgun.brandPrimary,
    borderRadius: 0,
    width: shotgun.deviceWidth,
    height: shotgun.deviceHeight,
    padding: shotgun.contentPadding
  },
  userInfoView: {
    height: 60
  },
  userContentTypesView: {
    height: 10
  },
  infoText: {
    fontSize: 10,
    marginLeft: 5,
    marginBottom: 10
  },
  actionButton: {
    marginBottom: shotgun.contentPadding
  },
  statusButton: {
    marginBottom: 10
  },
  subHeader: {
    color: shotgun.brandLight,
    fontSize: 16
  }
};

export default withExternalState(mapStateToProps)(UserDetail);

