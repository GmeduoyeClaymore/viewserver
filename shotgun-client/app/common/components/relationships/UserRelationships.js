import React, {Component}  from 'react';
import { withExternalState, ReduxRouter, Route } from 'custom-redux';
import {Tab, View, Text, Row, Switch, Header, Body, Title} from 'native-base';
import {Tabs, ErrorRegion, LoadingScreen} from 'common/components';
import { getDaoState, isAnyOperationPending, updateSubscriptionAction, getDaoSize, getOperationError, getDaoOptions } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {isEqual} from 'lodash';
import UserRelationshipMap from './UserRelationshipMap';
import UserRelationshipList from './UserRelationshipList';
import {updateRange, updateStatus} from 'common/actions/CommonActions';
import Slider from 'react-native-slider';
import UserRelationshipDetail from './UserRelationshipDetail';

const SubViewPath = 'RelationshipView/';

class UserRelationships extends Component{
  static InitialState = {
    showAll: true
  }
  constructor(props){
    super(props);
    this.UserViews = [
      {'Map': UserRelationshipMap},
      {'List': UserRelationshipList},
      {'Detail': UserRelationshipDetail, hidden: true},
    ];
    this.getOptionsFromProps = this.getOptionsFromProps.bind(this);
    this.setSelectedUser = this.setSelectedUser.bind(this);
    this.updateDistance = this.updateDistance.bind(this);
    this.setRange = this.setRange.bind(this);
    this.updateSubscription = this.updateSubscription.bind(this);
    this.getSelectedTabIndex = this.getSelectedTabIndex.bind(this);
    this.subscribeToUsers = this.subscribeToUsers.bind(this);
    this.goToTabNamed = this.goToTabNamed.bind(this);
  }

  beforeNavigateTo(){
    this.subscribeToUsers(this.getOptionsFromProps(this.props));
  }

  async subscribeToUsers(options){
    const {dispatch} = this.props;
    dispatch(updateSubscriptionAction('userRelationshipDao', options));
    this.oldOptions = options;
  }

  getOptionsFromProps(props){
    const {selectedProduct, geoLocation, showAll} = props;
    return {
      reportId: selectedProduct ? 'usersForProduct' : 'userRelationships',
      selectedProduct,
      position: geoLocation,
      showUnrelated: showAll,
      columnsToSort: [{name: 'distance', direction: 'asc'},  {name: 'ratingAvg', direction: 'desc'}, {name: 'firstName', direction: 'asc'}, {name: 'lastName', direction: 'asc'}]
    };
  }

  updateSubscription(){
    const newOptions = this.getOptionsFromProps(this.props);
    const {oldOptions} = this;
    if (!isEqual(newOptions, oldOptions, true)){
      this.subscribeToUsers(newOptions);
    }
  }

  componentWillReceiveProps(newProps){
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

  setSelectedUser(selectedUser){
    this.setState({selectedUser});
    const {history, path} = this.props;
    if (selectedUser){
      history.push(`${path}/${SubViewPath}DetailX`);
    }
  }

  goToTabNamed(name){
    const {history, path} = this.props;
    history.replace( `${path}/${SubViewPath}${name}X`);
  }

  getSelectedTabIndex(){
    const {history} = this.props;
    const currentPath = history.location.pathname;
    const indexOf =  currentPath.lastIndexOf(SubViewPath);
    if (!~indexOf){
      return 0;
    }
    const viewKey = currentPath.substring(indexOf + SubViewPath.length, currentPath.length - 1);
    const selectedIndex = this.UserViews.findIndex(c => Object.keys(c)[0] === viewKey);
    return !!~selectedIndex ? selectedIndex : 0;
  }

  async updateDistance(distance){
    this.setState({distance});
  }

  async setRange(distance){
    const {setRange} = this.props;
    setRange(distance);
  }

  render(){
    const {UserViews} = this;
    const {selectedUser, oldOptions, showAll, errors, noRelationships, me, searchText, selectedProduct, distance, parentPath, path, width, height} = this.props;
    if (!me){
      return <LoadingScreen text="Loading.."/>;
    }
    return <View style={{ flex: 1, padding: 10, maxWidth: width, maxHeight: height}}>
      <View style={styles.switchView}>
        {me ? <Slider step={1} minimumValue={0} maximumValue={50} value={distance} onSlidingComplete={this.setRange}/> : null}
      </View>
      {typeof noRelationships != 'undefined' ? <Row style={{height: 30}}>
        <Text style={{paddingTop: 6, flex: 2}}>
          {noRelationships + ' ' + (selectedProduct ? selectedProduct.name : '') + (showAll ? (selectedProduct ? 's' : ' users') : ' friends') + ' in ' + distance + ' miles ' + (searchText ? 'with name \"' + searchText + '\"' : '') }
        </Text>
        <View style={styles.friendsView}>
          <Text style={{marginRight: 5, paddingTop: 5}}>
            {showAll ? 'Everyone' : 'Friends'}
          </Text>
          <Switch style={styles.switch} onValueChange={ (value) => this.setState({ showAll: value }, () => this.updateSubscription())} value={ showAll }/>
        </View>
      </Row> : null}
      <Tabs style={{flex: 1, width}} page={this.getSelectedTabIndex()} {...shotgun.tabsStyle}>
        {UserViews.filter(v => !v.hidden).map(c => <Tab onPress={() => this.goToTabNamed(Object.keys(c)[0])} key={Object.keys(c)[0]} heading={Object.keys(c)[0]} />)}
      </Tabs>
      <View style={{flex: 24}}>
        <ErrorRegion errors={errors}>
          <ReduxRouter  name="UserRelationshipRouter"defaultRoute={`${SubViewPath}${Object.keys(UserViews[0])[0]}X`} {...this.props}  userRelationshipBasePath={path}  path={path} options={oldOptions} width={width}  selectedUser={selectedUser} setSelectedUser={this.setSelectedUser}>
            {UserViews.map( (c) => <Route width={150} key={Object.keys(c)[0]} parentPath={parentPath} path={`${SubViewPath}${Object.keys(c)[0]}X`} contentType={c} component={c[Object.keys(c)[0]]} />)}
          </ReduxRouter>
        </ErrorRegion>
      </View>
    </View>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const {dispatch} = initialProps;
  const setRange = (range) => dispatch(updateRange(range));
  const setStatus = ({status, statusMessage}) => dispatch(updateStatus({status, statusMessage}));
  const options = getDaoOptions(state, 'userRelationshipDao') || {};
  const {searchText} = options;

  const updateRelationshipError = getOperationError(state, 'userRelationshipDao', 'updateRelationship') || '';
  const updateRelationshipSubscriptionError = getOperationError(state, 'userRelationshipDao', 'updateSubscription') || '';

  return {
    ...initialProps,
    searchText,
    setRange,
    setStatus,
    noRelationships: getDaoSize(state, 'userRelationshipDao'),
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
      <Header>
        <Body><Title>{'Nearby Users'}</Title></Body>
      </Header>
      <UserRelationshipsControl {...this.props}/>
    </View>;
  }
}
const styles = {
  locationTextPlaceholder: {
    color: shotgun.silver
  },
  inputRow: {
    padding: shotgun.contentPadding
  },
  switch: {
    height: 30
  },
  friendsView: {
    flex: 1,
    justifyContent: 'flex-end',
    flexDirection: 'row'
  },
  switchView: {
  },
};
export default UserRelationshipsStandalone;


