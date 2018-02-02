import React from 'react';
import {connect} from 'custom-redux';
import {PagingListView, Tabs, OrderRequest} from 'common/components';
import { withRouter } from 'react-router';
import {View, Container, Spinner, Header, Body, Title, Tab, Text} from 'native-base';
import {isAnyLoading, getNavigationProps} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';

const CustomerOrders = ({history, isCompleted, userId}) => {
  const {location} = history;

  const reportOptions = {
    isCompleted,
    userId,
    orderId: undefined,
    columnsToSort: [{ name: 'from', direction: 'asc' }],
    reportId: 'customerOrderSummary'};

  const Paging = () => <Spinner />;
  const NoItems = () => <Text empty>No orders to display</Text>;
  const RowView = ({item: orderSummary, isLast, isFirst}) => {
    const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
    const next = isOnRoute ? '/Customer/CustomerOrderInProgress' : '/Customer/CustomerOrderDetail';
    return <OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} next={next} isLast={isLast} isFirst={isFirst}/>;
  };

  const onChangeTab = (newIsCompleted) => {
    if (isCompleted !== newIsCompleted) {
      history.replace(location.pathname, {isCompleted: newIsCompleted});
    }
  };

  return <Container>
    <Header hasTabs>
      <Body><Title>My Jobs</Title></Body>
    </Header>
    <Tabs initialPage={isCompleted === OrderStatuses.COMPLETED ? 1 : 0} {...shotgun.tabsStyle} onChangeTab={({i}) => onChangeTab(i == 1 ? OrderStatuses.COMPLETED : 'INCOMPLETE')}>
      <Tab heading="Live Jobs"/>
      <Tab heading="Complete"/>
    </Tabs>
    <View style={{flex: 1}}>
      <PagingListView
        daoName='orderSummaryDao'
        dataPath={['orders']}
        rowView={RowView}
        options={reportOptions}
        paginationWaitingView={Paging}
        emptyView={NoItems}
        pageSize={4}
        headerView={() => null}
      />
    </View>
  </Container>;
};

const mapStateToProps = (state, initialProps) => {
  const navigationProps = getNavigationProps(initialProps);
  return {
    ...initialProps,
    isCompleted: navigationProps.isCompleted !== undefined ? navigationProps.isCompleted : 'INCOMPLETE',
    busy: isAnyLoading(state, ['orderSummaryDao']),
  };
};

export default withRouter(connect(
  mapStateToProps
)(CustomerOrders));
