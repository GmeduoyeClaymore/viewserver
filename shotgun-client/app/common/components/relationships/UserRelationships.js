import React, {Component}  from 'react';
import { connect, setStateIfIsMounted } from 'custom-redux';
import {Button, Tab, View, Text, Row, Switch, Header, Left, Body, Title} from 'native-base';
import { withRouter } from 'react-router';
import {Tabs, ErrorRegion, Icon, LoadingScreen} from 'common/components';
import { getDaoState, isAnyOperationPending, updateSubscriptionAction, getDaoSize, getOperationError, getDaoOptions } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {isEqual} from 'lodash';
import UserRelationshipMap from './UserRelationshipMap';
import UserRelationshipList from './UserRelationshipList';
import {updateRange, updateStatus} from 'common/actions/CommonActions';
import Slider from 'react-native-slider';
import UserRelationshipDetail from './UserRelationshipDetail';

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
    this.setState = this.setState.bind(this);
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
    const {selectedProduct, geoLocation} = props;
    const {showAll} = this.parentState;
    return {
      reportId: selectedProduct ? 'usersForProduct' : 'userRelationships',
      selectedProduct,
      position: geoLocation,
      showUnrelated: showAll,
      columnsToSort: [{name: 'distance', direction: 'asc'},  {name: 'rating', direction: 'desc'}, {name: 'firstName', direction: 'asc'}, {name: 'lastName', direction: 'asc'}]
    };
  }

  updateSubscription(){
    const newOptions = this.getOptionsFromProps(this.props);
    const {oldOptions} = this.parentState;
    if (!isEqual(newOptions, oldOptions)){
      this.subscribeToUsers(newOptions);
    }
  }

  componentWillReceiveProps(newProps){
    const {oldOptions} = this.parentState;
    const newOptions = this.getOptionsFromProps(newProps);
    if (!isEqual(newOptions, oldOptions)){
      this.subscribeToUsers(newOptions);
    }
    const {me} = newProps;
    if (me && me.range  !== this.parentState.distance){
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

  setState(newState, continueWith){
    const {context} = this.props;
    if (context){
      return context.setState(newState, continueWith);
    }
    super.setState(newState, continueWith);
  }

  get parentState(){
    const {context} = this.props;
    if (context){
      return context.state;
    }
    return this.state;
  }

  render(){
    const {onChangeTab, UserViews, parentState} = this;
    const {history, errors, noRelationships, me, searchText, title, selectedProduct, backAction} = this.props;
    const {selectedUser, selectedUserIndex, selectedTabIndex = 0, oldOptions, showAll} = parentState;
    const UserViewRecord = UserViews[selectedTabIndex];
    const UserView = UserViewRecord[Object.keys(UserViewRecord)[0]];
    if (!me){
      return <LoadingScreen text="Loading.."/>;
    }
    return <View style={{ flex: 1, padding: 15}}>
      {title ?  <Header withButton>
        <Left>
          <Button onPress={() => backAction ? backAction() : history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{title || 'Nearby Users'}</Title></Body>
      </Header> : null}
      <View style={{...styles.container, marginBottom: 10}}>
        {me ? <Slider step={1} minimumValue={0} maximumValue={50} value={parentState.distance} onSlidingComplete={this.setRange}/> : null}
      </View>
      {typeof noRelationships != 'undefined' ? <Row style={{height: 30}}>
        <Text style={{paddingTop: 6, flex: 2}}>
          {noRelationships + ' ' + (selectedProduct ? selectedProduct.name : '') + (showAll ? (selectedProduct ? 's' : ' users') : ' friends') + ' in ' + parentState.distance + 'miles ' + (searchText ? 'with name \"' + searchText + '\"' : '') }
        </Text>
        <View style={{flex: 1, justifyContent: 'flex-end', flexDirection: 'row'}}>
          <Text style={{marginRight: 5, paddingTop: 5}}>
            {parentState.showAll ? 'Everyone' : 'Friends'}
          </Text>
          <Switch style={{height: 30}} onValueChange={ (value) => this.setState({ showAll: value }, () => this.updateSubscription())} value={ parentState.showAll }/>
        </View>
      </Row> : null}
      <Tabs  style={{flex: 1}} initialPage={selectedTabIndex} {...shotgun.tabsStyle} onChangeTab={({ i }) => onChangeTab(i)}>
        {UserViews.map(c => <Tab key={Object.keys(c)[0]} heading={Object.keys(c)[0]} />)}
      </Tabs>
      <View style={{flex: 24}}>
        <ErrorRegion errors={errors}>
          <UserView {...this.props} context={this} options={oldOptions} selectedUser={selectedUser}/>
        </ErrorRegion>
      </View>
      <UserRelationshipDetail ref={detail => {this.detail = detail;}} {...this.props} context={this} selectedUser={selectedUser} selectedUserIndex={selectedUserIndex}  />
    </View>;
  }
}

const styles = {
  backButton: {
    position: 'absolute',
    left: 0,
    top: 10
  },
  title: {
    fontWeight: 'bold',
    color: '#848484',
    fontSize: 18,
    marginTop: 30
  },
  locationTextPlaceholder: {
    color: shotgun.silver
  },
  inputRow: {
    padding: shotgun.contentPadding
  },
  container: {
    flex: 1,
    marginTop: 10,
    marginLeft: 0,
    marginRight: 10,
    alignItems: 'stretch',
    justifyContent: 'center',
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

export default withRouter(connect(
  mapStateToProps
)(UserRelationships));


