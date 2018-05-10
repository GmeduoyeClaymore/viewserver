import React, {Component}  from 'react';
import { withExternalState, ReduxRouter, Route } from 'custom-redux';
import {Button, Tab, View, Text, Row, Col, Grid, Switch, Header, Body, Title} from 'native-base';
import {Tabs, ErrorRegion, LoadingScreen} from 'common/components';
import { getDaoState, isAnyOperationPending, updateSubscriptionAction, getDaoSize, getOperationError, getDaoOptions } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {isEqual} from 'lodash';
import UserRelationshipMap from './UserRelationshipMap';
import {updateRange, updateStatus} from 'common/actions/CommonActions';
import UserRelationshipDetail from './UserRelationshipDetail';

const SubViewPath = 'RelationshipView/';

class UserRelationships extends Component{
  constructor(props){
    super(props);
    this.UserViews = [
      {'Map': UserRelationshipMap},
      {'Detail': UserRelationshipDetail, hidden: true},
    ];
  }

  componentWillReceiveProps = (newProps) => {
    const {distance} = newProps;
    const {oldOptions} = this;
    const newOptions = this.getOptionsFromProps(newProps);
    if (!isEqual(newOptions, oldOptions, true)){
      this.subscribeToUsers(newOptions);
    }
    const {me} = newProps;
    if (me && me.range  !== distance){
      this.setState({distance: me.range});
    }
  }

  beforeNavigateTo = () => {
    this.subscribeToUsers(this.getOptionsFromProps(this.props));
  }

  subscribeToUsers = async(options) => {
    const {dispatch} = this.props;
    dispatch(updateSubscriptionAction('userRelationshipDao', options));
    this.oldOptions = options;
  }

  getOptionsFromProps = (props) => {
    const {selectedProduct, geoLocation, showAll} = props;
    return {
      reportId: selectedProduct ? 'usersForProduct' : 'userRelationships',
      selectedProduct,
      position: geoLocation,
      showUnrelated: showAll,
      columnsToSort: [{name: 'distance', direction: 'asc'},  {name: 'ratingAvg', direction: 'desc'}, {name: 'firstName', direction: 'asc'}, {name: 'lastName', direction: 'asc'}]
    };
  }

  updateSubscription = () => {
    const newOptions = this.getOptionsFromProps(this.props);
    const {oldOptions} = this;
    if (!isEqual(newOptions, oldOptions, true)){
      this.subscribeToUsers(newOptions);
    }
  }

  setSelectedUser = (selectedUser) => {
    this.setState({selectedUser});
    const {history, path} = this.props;
    if (selectedUser){
      history.push(`${path}/${SubViewPath}DetailX`);
    }
  }

  setShowAll = (showAll) => {
    this.setState({showAll}, () => this.updateSubscription());
  }

  render(){
    const {UserViews} = this;
    const {selectedUser, oldOptions, me, parentPath, path, showAll} = this.props;
    if (!me){
      return <LoadingScreen text="Loading.."/>;
    }
    return <Grid>
      <Row>
        <Row style={styles.showAllView}>
          <Button style={styles.showAllButton} light={showAll} onPress={() => this.setShowAll(false)}>
            <Text style={styles.buttonText}>Friends</Text>
          </Button>
          <Button style={styles.showAllButton} light={!showAll} onPress={() => this.setShowAll(true)}>
            <Text style={styles.buttonText}>Everyone</Text>
          </Button>
        </Row>

        <ReduxRouter  name="UserRelationshipRouter" defaultRoute={`${SubViewPath}${Object.keys(UserViews[0])[0]}X`} {...this.props}  userRelationshipBasePath={path}  path={path} options={oldOptions} selectedUser={selectedUser} setSelectedUser={this.setSelectedUser}>
          {UserViews.map( (c) => <Route key={Object.keys(c)[0]} parentPath={parentPath} path={`${SubViewPath}${Object.keys(c)[0]}X`} contentType={c} component={c[Object.keys(c)[0]]} />)}
        </ReduxRouter>
      </Row>
    </Grid>;
  }
}

const styles = {
  showAllView: {
    position: 'absolute',
    top: 10,
    right: 15,
    zIndex: 3
  },
  showAllButton: {
    marginLeft: 5,
    justifyContent: 'center',
    width: 100
  },
  buttonText: {
    fontSize: 10
  }
};

const mapStateToProps = (state, initialProps) => {
  const options = getDaoOptions(state, 'userRelationshipDao') || {};
  const {searchText} = options;

  const updateRelationshipError = getOperationError(state, 'userRelationshipDao', 'updateRelationship') || '';
  const updateRelationshipSubscriptionError = getOperationError(state, 'userRelationshipDao', 'updateSubscription') || '';

  return {
    ...initialProps,
    searchText,
    errors: updateRelationshipError + '\n' + updateRelationshipSubscriptionError,
    me: getDaoState(state, ['user'], 'userDao'),
    relatedUsers: getDaoState(state, ['users'], 'userRelationshipDao') || [],
    busy: isAnyOperationPending(state, [{userRelationshipDao: 'updateSubscription'}])
  };
};

export const UserRelationshipsControl =  withExternalState(mapStateToProps)(UserRelationships);

export class UserRelationshipsStandalone extends Component{
  constructor(props){
    super(props);
  }

  render(){
    return <View style={{ flex: 1}}>
      <Title style={{backgroundColor: 'transparent', position: 'absolute', top: 10, zIndex: 3}}>{'Nearby Users'}</Title>
      <UserRelationshipsControl {...this.props}/>
    </View>;
  }
}

export default UserRelationshipsStandalone;


