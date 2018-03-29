import React, {Component}  from 'react';
import { connect, setStateIfIsMounted } from 'custom-redux';
import {Button, Tab, View, Text, Row, Switch, Header, Left, Body, Title} from 'native-base';
import {Tabs, ErrorRegion, Icon, LoadingScreen} from 'common/components';
import { getDaoState, isAnyOperationPending, updateSubscriptionAction, getDaoSize, getOperationError, getDaoOptions } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {isEqual} from 'lodash';
import UserRelationshipMap from './UserRelationshipMap';
import UserRelationshipList from './UserRelationshipList';
import {updateRange, updateStatus} from 'common/actions/CommonActions';
import Slider from 'react-native-slider';
import UserRelationshipDetail from './UserRelationshipDetail';
import {withExternalState} from 'custom-redux';

class UserRelationships extends Component{
  constructor(props){
    super(props);
    this.state = {
      showAll: true,
      selectedUserIndex: -1
    };
    setStateIfIsMounted(this);
    this.UserViews = [
      {'Map': UserRelationshipMap},
      {'List': UserRelationshipList},
    ];
    this.getOptionsFromProps = this.getOptionsFromProps.bind(this);
    this.onChangeTab = this.onChangeTab.bind(this);
    this.setSelectedUser = this.setSelectedUser.bind(this);
    this.updateDistance = this.updateDistance.bind(this);
    this.setRange = this.setRange.bind(this);
    this.updateSubscription = this.updateSubscription.bind(this);
  }

  componentDidMount(){
    this.subscribeToUsers(this.getOptionsFromProps(this.props));
  }

  async subscribeToUsers(options){
    const {dispatch} = this.props;
    dispatch(updateSubscriptionAction('userRelationshipDao', options));
    this.setState({oldOptions: options});
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
    const {oldOptions} = this.props;
    if (!isEqual(newOptions, oldOptions)){
      this.subscribeToUsers(newOptions);
    }
  }

  componentWillReceiveProps(newProps){
    const {oldOptions, distance} = newProps;
    const newOptions = this.getOptionsFromProps(newProps);
    if (!isEqual(newOptions, oldOptions)){
      this.subscribeToUsers(newOptions);
    }
    const {me} = newProps;
    if (me && me.range  !== distance){
      this.setState({distance: me.range});
    }
  }

  setSelectedUser(selectedUser){
    this.setState({selectedUser});
    if (selectedUser){
      this.detail.wrappedInstance.show();
    }
  }

  onChangeTab(selectedTabIndex){
    this.setState({selectedTabIndex});
  }

  async updateDistance(distance){
    this.setState({distance});
  }

  async setRange(distance){
    const {setRange} = this.props;
    setRange(distance);
  }

  render(){
    const {onChangeTab, UserViews} = this;
    const {selectedUser, selectedUserIndex, selectedTabIndex = 0, oldOptions, showAll, history, errors, noRelationships, me, searchText, title, selectedProduct, backAction, distance} = this.props;
    const UserViewRecord = UserViews[selectedTabIndex];
    const UserView = UserViewRecord[Object.keys(UserViewRecord)[0]];
    if (!me){
      return <LoadingScreen text="Loading.."/>;
    }
    return <View style={{ flex: 1}}>
      <Header withButton={title != undefined}>
        {title ? <Left>
          <Button onPress={() => backAction ? backAction() : history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left> : null}
        <Body><Title>{title || 'Nearby Users'}</Title></Body>
      </Header>
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
      <Tabs style={{flex: 1}} initialPage={selectedTabIndex} {...shotgun.tabsStyle} onChangeTab={({ i }) => onChangeTab(i)}>
        {UserViews.map(c => <Tab key={Object.keys(c)[0]} heading={Object.keys(c)[0]} />)}
      </Tabs>
      <View style={{flex: 24}}>
        <ErrorRegion errors={errors}>
          <UserView {...this.props} options={oldOptions} selectedUser={selectedUser} setSelectedUser={this.setSelectedUser}/>
        </ErrorRegion>
      </View>
      <UserRelationshipDetail ref={detail => {this.detail = detail;}} {...this.props} selectedUser={selectedUser} selectedUserIndex={selectedUserIndex}  />
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
    paddingLeft: 20,
    paddingRight: 20,
    marginTop: 10,
    marginBottom: 10
  },
};

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

export default withExternalState(mapStateToProps)(UserRelationships);


