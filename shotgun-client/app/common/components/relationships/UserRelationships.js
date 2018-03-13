import React, {Component}  from 'react';
import { connect, setStateIfIsMounted } from 'custom-redux';
import { Container, Button, Tab, View, Text, Row, Picker, Input, Switch} from 'native-base';
import { withRouter } from 'react-router';
import {Tabs, ErrorRegion, Icon, LoadingScreen} from 'common/components';
import { getDaoState, isAnyOperationPending, updateSubscriptionAction, getDaoSize, getOperationError } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {isEqual} from 'lodash';
import UserRelationshipMap from './UserRelationshipMap';
import UserRelationshipList from './UserRelationshipList';
import {updateRange, updateStatus} from 'common/actions/CommonActions';
import Slider from 'react-native-slider';

class UserRelationships extends Component{
  constructor(props){
    super(props);
    this.state = {};
    setStateIfIsMounted(this);
    this.UserViews = [
      {'Map': UserRelationshipMap},
      {'List': UserRelationshipList},
    //  {'Detail': UserRelationshipDetail},
    ];
    this.getOptionsFromProps = this.getOptionsFromProps.bind(this);
    this.setState = this.setState.bind(this);
    this.onChangeTab = this.onChangeTab.bind(this);
    this.updateStatusMessage = this.updateStatusMessage.bind(this);
    this.updateDistance = this.updateDistance.bind(this);
    this.setRange = this.setRange.bind(this);
    this.setStatus = this.setStatus.bind(this);
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
    const {reportId = 'userRelationships', productId} = props;
    const {showAll} = this.state;
    return {
      reportId,
      productId,
      showUnrelated: showAll,
      columnsToSort: [{name: 'distance', direction: 'asc'}]
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
    if (me && me.statusMessage  !== this.state.statusMessage){
      this.setState({statusMessage: me.statusMessage});
    }
    if (me && me.range  !== this.state.distance){
      this.setState({distance: me.range});
    }
  }

  setSelectedUser(selectedUser){
    this.setState({selectedUser, selectedTabIndex: 2});
  }

  onChangeTab(selectedTabIndex){
    this.setState({selectedTabIndex});
  }

  updateStatusMessage(statusMessage){
    this.setState({statusMessage});
  }

  setStatus(args){
    const {statusMessage, status, setStatus} = this.props;
    const payload = {statusMessage, status, ...args};
    setStatus(payload);
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
    const {history, errors, noRelationships, me} = this.props;
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
          {noRelationships + ' ' + (showAll ? 'users' : 'friends') + ' found in ' + state.distance + 'miles'}
        </Text>
        <Switch onValueChange={ (value) => this.setState({ showAll: value }, () => this.updateSubscription())} value={ this.state.showAll }/>
      </Row> : null}
      <Row style={{flex: 1}}>
        <Picker
          iosHeader="Select one"
          mode="dropdown"
          selectedValue={me.status}
          onValueChange={(status) => this.setStatus({status})}>
          <Picker.Item label="Online" value="ONLINE" />
          <Picker.Item label="Busy" value="BUSY" />
          <Picker.Item label="Appear Offline" value="OFFLINE" />
        </Picker>
      </Row>
      <Row style={{flex: 1}}>
        <Input value={state.statusMessage} placeholder="What are you up to ?" placeholderTextColor={shotgun.silver} onChangeText={value => this.updateStatusMessage(value)} onBlur={() => this.setStatus({statusMessage: state.statusMessage})}/>
      </Row>
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
  return {
    ...initialProps,
    state,
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


