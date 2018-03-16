import React, {Component}  from 'react';
import { connect, setStateIfIsMounted } from 'custom-redux';
import { Container, Button, Tab, View, Text, Row, Picker, Input, Switch} from 'native-base';
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
    this.state = {};
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
    const {selectedProduct = {}, location = {}} = props;
    const {showAll} = this.state;
    const {latitude, longitude} = location;
    const {productId} = selectedProduct;
    return {
      reportId: productId ? 'usersForProduct' : 'userRelationships',
      productId,
      latitude,
      longitude,
      showUnrelated: showAll,
      columnsToSort: [{name: 'distance', direction: 'asc'},  {name: 'rating', direction: 'desc'}, {name: 'firstName', direction: 'asc'}, {name: 'lastName', direction: 'asc'}]
    };
  }

  updateSubscription(){
    const newOptions = this.getOptionsFromProps(this.props);
    const {oldOptions} = this.state;
    if (!isEqual(newOptions, oldOptions)){
      this.subscribeToUsers(newOptions);
    }
  }

  componentWillReceiveProps(newProps){
    const {oldOptions} = this.state;
    const newOptions = this.getOptionsFromProps(newProps);
    if (!isEqual(newOptions, oldOptions)){
      this.subscribeToUsers(newOptions);
    }
    const {me} = newProps;
    if (me && me.range  !== this.state.distance){
      this.setState({distance: me.range});
    }
  }

  setSelectedUser(selectedUser){
    this.setState({selectedUser});
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
    const {onChangeTab, state, UserViews} = this;
    const {history, errors, noRelationships, me, searchText} = this.props;
    const {selectedUser, selectedTabIndex = 0, oldOptions, showAll} = state;
    const UserViewRecord = UserViews[selectedTabIndex];
    const UserView = UserViewRecord[Object.keys(UserViewRecord)[0]];
    if (!me){
      return <LoadingScreen text="Loading.."/>;
    }
    return <Container style={{ flex: 1, padding: 15}}>
      <View style={styles.container}>
        {me ? <Slider step={1} minimumValue={0} maximumValue={50} value={state.distance} onSlidingComplete={this.setRange}/> : null}
      </View>
      {typeof noRelationships != undefined ? <Row style={{flex: 1}}>
        <Text>
          {noRelationships + ' ' + (showAll ? 'users' : 'friends') + ' found in ' + state.distance + 'miles ' + (searchText ? 'with name \"' + searchText + '\"' : '') }
        </Text>
        <Switch onValueChange={ (value) => this.setState({ showAll: value }, () => this.updateSubscription())} value={ this.state.showAll }/>
      </Row> : null}
      <Tabs  style={{flex: 1}} initialPage={selectedTabIndex} {...shotgun.tabsStyle} onChangeTab={({ i }) => onChangeTab(i)}>
        {UserViews.map(c => <Tab key={Object.keys(c)[0]} heading={Object.keys(c)[0]} />)}
      </Tabs>
      <View style={{flex: 14}}>
        <ErrorRegion errors={errors}>
          <UserView {...this.props} context={this} options={oldOptions} selectedUser={selectedUser}/>
        </ErrorRegion>
      </View>
      <Button transparent style={styles.backButton} onPress={() => history.goBack()} >
        <Icon name='back-arrow'/>
      </Button>
      <UserRelationshipDetail {...this.props} context={this} selectedUser={selectedUser}/>
    </Container>;
  }
}

const styles = {
  backButton: {
    position: 'absolute',
    left: 0,
    top: 0
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
    marginLeft: 10,
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

  return {
    ...initialProps,
    searchText,
    setRange,
    setStatus,
    noRelationships: getDaoSize(state, 'userRelationshipDao'),
    errors: getOperationError(state, 'userRelationshipDao', 'updateRelationship'),
    me: getDaoState(state, ['user'], 'userDao'),
    relatedUsers: getDaoState(state, ['users'], 'userRelationshipDao') || [],
    busy: isAnyOperationPending(state, [{userRelationshipDao: 'updateSubscription'}])
  };
};

export default withRouter(connect(
  mapStateToProps
)(UserRelationships));


