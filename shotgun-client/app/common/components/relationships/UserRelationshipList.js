import React, {Component}  from 'react';
import {updateSubscriptionAction } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import UserRelationshipItem from './UserRelationshipItem';
const headerView  = ({options: opts, search}) => <SearchBar onChange={search} text={opts.searchText} style={{marginBottom: 15}}/>;
const NoItems = () => <View><Text>No items to display</Text></View>;
export default class UserRelationshipList extends Component{
  constructor(props){
    super(props);
    this.rowView = ({item: user, ...rest}) => {
      return (<UserRelationshipItem key={user.userId} user={user} {...rest}/>);
    };
    this.search = (searchText) => {
      dispatch(updateSubscriptionAction('userRelationshipDao', {searchText}));
    };
    this.search = this.search.bind(this);
  }
  
  render(){
    const {rowView, props, search} = this;
    const {selectedUser, context} = props;
    return <PagingListView
      daoName='userRelationshipDao'
      dataPath={['users']}
      context={context}
      rowView={rowView}
      selectedUser={selectedUser}
      search={search}
      paginationWaitingView={Paging}
      emptyView={NoItems}
      headerView={headerView}
    />;
  }
}
