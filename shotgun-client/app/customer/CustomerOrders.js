import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {PagingListView, Tabs, OrderRequest} from 'common/components';
import { withRouter } from 'react-router';
import {View, Container, Spinner, Header, Body, Title, Tab, Text} from 'native-base';
import {isAnyLoading, getNavigationProps, resetSubscriptionAction} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';
const Paging = () => <Spinner />;
const NoItems = () => <Text empty>No orders to display</Text>;
const RowView = ({item: orderSummary, isLast, isFirst}) => {
  const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
  const next = isOnRoute ? '/Customer/CustomerOrderInProgress' : '/Customer/CustomerOrderDetail';
  return <OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} next={next} isLast={isLast} isFirst={isFirst}/>;
};

const CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS = {
  columnsToSort: [{ name: 'from', direction: 'asc' }, { name: 'orderId', direction: 'asc' }],
  reportId: 'customerOrderSummary'
};

class CustomerOrders extends Component{
  constructor(props){
    super(props);
    this.onChangeTab = this.onChangeTab.bind(this);
  }

  componentWillMount(){
    const {resetOrders} = this.props;
    if (resetOrders){
      resetOrders();
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


  render(){
    const {isCompleted, defaultOptions} = this.props;
    const {onChangeTab} = this;
  
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
          options={defaultOptions}
          paginationWaitingView={Paging}
          emptyView={NoItems}
          pageSize={10}
          headerView={() => null}
        />
      </View>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const navigationProps = getNavigationProps(initialProps);
  const {dispatch} = initialProps;
  const {isCompleted = false} = navigationProps;
  const defaultOptions = {
    ...CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS,
    isCompleted
  };
  const resetOrders = () => {
    dispatch(resetSubscriptionAction('orderSummaryDao', defaultOptions));
  };
  return {
    ...initialProps,
    resetOrders,
    defaultOptions,
    isCompleted,
    busy: isAnyLoading(state, ['orderSummaryDao']),
  };
};

export default withRouter(connect(
  mapStateToProps
)(CustomerOrders));
