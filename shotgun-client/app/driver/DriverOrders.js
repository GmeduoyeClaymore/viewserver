import React from 'react';
import {connect} from 'custom-redux';
import {View, Text} from 'react-native';
import {PagingListView, OrderRequest, Tabs} from 'common/components';
import { withRouter } from 'react-router';
import {Container, Spinner, Header, Body, Title, Tab} from 'native-base';
import {getDaoState} from 'common/dao';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import shotgun from 'native-base-theme/variables/shotgun';

const DriverOrders = ({history, isCompleted, userId}) => {
  const {location} = history;

  const reportOptions = {
    isCompleted: isCompleted ? OrderStatuses.COMPLETED : 'INCOMPLETE',
    columnsToSort: [{ name: 'from', direction: 'asc' }],
    orderId: undefined,
    userId,
    reportId: 'driverOrderSummary'};

  const Paging = () => <View style={{flex: 1}}><Spinner /></View>;
  const NoItems = () => <View style={{flex: 1, display: 'flex'}}><Text>No jobs assigned</Text></View>;
  const RowView = ({item: orderSummary, isLast, isFirst}) => <OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} isLast={isLast} isFirst={isFirst} next='/Driver/DriverOrderDetail'/>;

  const onChangeTab = (newIsCompleted) => {
    if (isCompleted !== newIsCompleted) {
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

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  isCompleted: initialProps.history.location.state && initialProps.history.location.state.isCompleted !== undefined ? initialProps.history.location.state.isCompleted : false,
  vehicle: getDaoState(state, ['vehicle'], 'vehicleDao'),
  position: getDaoState(state, ['position'], 'userDao')
});

export default withRouter(connect(
  mapStateToProps )(DriverOrders));
