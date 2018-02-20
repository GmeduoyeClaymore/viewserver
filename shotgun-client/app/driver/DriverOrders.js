import React from 'react';
import {connect} from 'custom-redux';
import {PagingListView, OrderRequest, Tabs} from 'common/components';
import { withRouter } from 'react-router';
import {View, Text, Container, Spinner, Header, Body, Title, Tab} from 'native-base';
import {getDaoState, getNavigationProps} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';

const DriverOrders = ({history, isCompleted, userId}) => {
  const {location} = history;

  const reportOptions = {
    isCompleted,
    columnsToSort: [{ name: 'from', direction: 'asc' }],
    driverId: userId,
    reportId: 'driverOrderSummary'};

  const Paging = () => <Spinner />;
  const NoItems = () => <Text empty>{isCompleted ? 'You have no completed jobs' : 'You have no live jobs'}</Text>;
  const RowView = ({item: orderSummary, isLast, isFirst}) => <OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} isLast={isLast} isFirst={isFirst} next='/Driver/DriverOrderDetail'/>;

  const onChangeTab = (newIsCompleted) => {
    if (isCompleted !== newIsCompleted) {
      if (this.pagingListView){
        this.pagingListView.wrappedInstance.reset();
      }
      history.replace(location.pathname, {isCompleted: newIsCompleted});
    }
  };

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
        options={reportOptions}
        paginationWaitingView={Paging}
        emptyView={NoItems}
        pageSize={10}
        headerView={() => null}
      />
    </View>
  </Container>;
};

const mapStateToProps = (state, initialProps) => {
  const navigationProps = getNavigationProps(initialProps);
  return {
    ...initialProps,
    isCompleted: navigationProps.isCompleted !== undefined ? navigationProps.isCompleted : false,
    vehicle: getDaoState(state, ['vehicle'], 'vehicleDao'),
    position: getDaoState(state, ['position'], 'userDao')
  };
};

export default withRouter(connect(
  mapStateToProps )(DriverOrders));
