import React, {Component}  from 'react';
import { connect, setStateIfIsMounted } from 'custom-redux';
import { Container, Button, Tab, View} from 'native-base';
import { withRouter } from 'react-router';
import {Tabs, ErrorRegion, Icon} from 'common/components';
import { getDaoState, isAnyOperationPending, updateSubscriptionAction } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {isEqual} from 'lodash';
import UserRelationshipMap from './UserRelationshipMap';
import UserRelationshipList from './UserRelationshipList';
import UserRelationshipDetail from './UserRelationshipDetail';

class UserRelationships extends Component{
  constructor(props){
    super(props);
    this.state = {};
    setStateIfIsMounted(this);
    this.UserViews = [
      {'Map': UserRelationshipMap},
      {'List': UserRelationshipList},
      {'Detail': UserRelationshipDetail},
    ];
    this.getOptionsFromProps = this.getOptionsFromProps.bind(this);
    this.setState = this.setState.bind(this);
    this.onChangeTab = this.onChangeTab.bind(this);
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
    return {
      reportId,
      productId,
      columnsToSort: [{name: 'distance', direction: 'asc'}]
    };
  }

  componentWillReceiveProps(newProps){
    const {oldOptions} = this.state;
    const newOptions = this.getOptionsFromProps(newProps);
    if (!isEqual(newOptions, oldOptions)){
      this.subscribeToUsers(newOptions);
    }
  }

  setSelectedUser(selectedUser){
    this.setState({selectedUser, selectedTabIndex: 2});
  }

  onChangeTab(selectedTabIndex){
    this.setState({selectedTabIndex});
  }

  render(){
    const {onChangeTab, state, UserViews} = this;
    const {history, errors} = this.props;
    const {selectedUser, selectedTabIndex = 0, oldOptions} = state;
    const UserViewRecord = UserViews[selectedTabIndex];
    const UserView = UserViewRecord[Object.keys(UserViewRecord)[0]];
    return <Container style={{ flex: 1, padding: 15}}>
      <Tabs initialPage={selectedTabIndex} {...shotgun.tabsStyle} onChangeTab={({ i }) => onChangeTab(i)}>
        {UserViews.map(c => <Tab key={Object.keys(c)[0]} heading={Object.keys(c)[0]} />)}
      </Tabs>
      <View style={{flex: 1}}>
        <ErrorRegion errors={errors}>
          <UserView {...this.props} context={this} options={oldOptions} selectedUser={selectedUser}/>
        </ErrorRegion>
      </View>
      <Button transparent style={styles.backButton} onPress={() => history.back()} >
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
  }
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps,
    state,
    me: getDaoState(state, ['user'], 'userDao'),
    relatedUsers: getDaoState(state, ['users'], 'userRelationshipDao') || [],
    busy: isAnyOperationPending(state, [{userRelationshipDao: 'updateSubscription'}])
  };
};

export default withRouter(connect(
  mapStateToProps
)(UserRelationships));


