import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {PagingListView, Tabs, OrderRequest} from 'common/components';
import { withRouter } from 'react-router';
import {View, Container, Spinner, Header, Body, Title, Tab, Text} from 'native-base';
import {isAnyLoading, getNavigationProps} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';

const Paging = () => <Spinner />;
const NoItems = () => <Text empty>No orders to display</Text>;
const RowView = ({item: orderSummary, isLast, isFirst}) => {
  const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
  const next = isOnRoute ? '/Customer/CustomerOrderInProgress' : '/Customer/CustomerOrderDetail';
  return <OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} next={next} isLast={isLast} isFirst={isFirst}/>;
};

class CustomerOrders extends Component{
  constructor(props){
    super(props);
    this.onChangeTab = this.onChangeTab.bind(this);
    this.getReportOptions = this.getReportOptions.bind(this);
  }

  componentDidMount(){
    if (this.pagingListView){
      this.pagingListView.wrappedInstance.reset();
    }
  }

  onChangeTab(newIsCompleted){
    const {history, isCompleted} = this.props;
    const {location} = history;
    if (isCompleted !== newIsCompleted) {
      if (this.pagingListView){
        this.pagingListView.wrappedInstance.reset();
      }
      history.replace(location.pathname, {isCompleted: newIsCompleted});
    }
  }
  getReportOptions(){
    const {isCompleted} = this.props;
    return  {
      isCompleted,
      columnsToSort: [{ name: 'from', direction: 'asc' }],
      reportId: 'customerOrderSummary'};
  }

  render(){
    const {isCompleted} = this.props;
    const {onChangeTab, getReportOptions} = this;
  
    return <Container>
      <Header hasTabs>
        <Body><Title>My Jobs</Title></Body>
      </Header>
      <Tabs initialPage={isCompleted ? 1 : 0} {...shotgun.tabsStyle} onChangeTab={({i}) => onChangeTab(i == 1)}>
        <Tab heading="Live Jobs"/>
        <Tab heading="Complete"/>
      </Tabs>
      <View style={{flex: 1}}>
        <PagingListView
          ref={ c => {this.pagingListView = c;}}
          daoName='orderSummaryDao'
          dataPath={['orders']}
          rowView={RowView}
          options={getReportOptions()}
          paginationWaitingView={Paging}
          emptyView={NoItems}
          pageSize={4}
          headerView={() => null}
        />
      </View>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const navigationProps = getNavigationProps(initialProps);
  return {
    ...initialProps,
    isCompleted: navigationProps.isCompleted !== undefined ? navigationProps.isCompleted : false,
    busy: isAnyLoading(state, ['orderSummaryDao']),
  };
};

export default withRouter(connect(
  mapStateToProps
)(CustomerOrders));
