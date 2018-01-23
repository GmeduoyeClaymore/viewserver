import React from 'react';
import {connect} from 'react-redux';
import {View, Text} from 'react-native';
import PagingListView from 'common/components/PagingListView';
import { withRouter } from 'react-router';
import {Container, Content, Spinner, Header, Body, Title, Tab, List} from 'native-base';
import Tabs from 'common/components/Tabs';
import {isAnyLoading} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import OrderRequest from 'common/components/OrderRequest';
import CustomerOrderCta from './components/CustomerOrderCta';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import LoadingScreen from 'common/components/LoadingScreen';

const CustomerOrders = ({history, isCompleted, userId, busy}) => {
  const {location} = history;

  const reportOptions = {
    isCompleted,
    userId,
    orderId: undefined,
    columnsToSort: [{ name: 'eta', direction: 'asc' }],
    reportId: 'customerOrderSummary'};

  const Paging = () => <View style={{flex: 1}}><Spinner /></View>;
  const NoItems = () => <View style={{flex: 1, display: 'flex'}}><Text>No orders to display</Text></View>;
  const RowView = (orderSummary) => [<OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} next='/Customer/CustomerOrderDetail'/>, <CustomerOrderCta key={`${orderSummary.orderId}Cta`} orderSummary={orderSummary}/>];

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
    <Content>
      <List>
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
      </List>
    </Content>
  </Container>;
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  isCompleted: initialProps.history.location.state && initialProps.history.location.state.isCompleted !== undefined ? initialProps.history.location.state.isCompleted : 'INCOMPLETE',
  busy: isAnyLoading(state, ['orderSummaryDao']),
});

export default withRouter(connect(
  mapStateToProps
)(CustomerOrders));
