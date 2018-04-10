import React, {Component}  from 'react';
import {updateSubscriptionAction, getOperationError} from 'common/dao';
import UserRelationshipItem from './UserRelationshipItem';
import {PagingListView, SearchBar} from 'common/components';
import {View, Spinner} from 'native-base';
import {Text} from 'react-native';
import {connect} from 'custom-redux';
import {callUser} from 'common/actions/CommonActions';
import PhoneCallService from 'common/services/PhoneCallService';

const Paging = () => <View><Spinner /></View>;
const headerView  = ({options: opts = {}, search}) => <SearchBar onChange={search} text={opts.searchText} style={{marginBottom: 15}}/>;
const NoItems = () => <View><Text>No items to display</Text></View>;
export class UserRelationshipList extends Component{
  constructor(props){
    super(props);
    this.rowView = ({item: user, ...rest}) => {
      return (<UserRelationshipItem key={user.userId} user={user} {...rest}/>);
    };
    this.search = (searchText) => {
      const {dispatch} = this.props;
      dispatch(updateSubscriptionAction('userRelationshipDao', {searchText}));
    };
    this.search = this.search.bind(this);
  }
  
  render(){
    const {rowView, props, search} = this;
    const {selectedUser, options, onPressCallUser, setSelectedUser} = props;
    return <PagingListView
      daoName='userRelationshipDao'
      dataPath={['users']}
      elementContainerStyle={{borderWidth: 0.5, borderColor: '#edeaea'}}
      setSelectedUser={setSelectedUser}
      onPressCallUser={onPressCallUser}
      options={ options}
      rowView={rowView}
      selectedUser={selectedUser}
      search={search}
      paginationWaitingView={Paging}
      emptyView={NoItems}
      headerView={headerView}
    />;
  }
}

const mapStateToProps = (state, initialProps) => {
  const {dispatch} = initialProps;
  const onPressCallUser = async (user) => {
    const {userId, relationshipStatus} = user;
    if (relationshipStatus === 'ACCEPTED'){
      PhoneCallService.call(user.contactNo);
    } else {
      dispatch(callUser({userId}));
    }
  };
  return {
    ...initialProps,
    onPressCallUser,
    errors: getOperationError(state, 'userRelationshipDao', 'callUser'),
  };
};

export default connect(
  mapStateToProps
)(UserRelationshipList);
