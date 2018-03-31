import React, {Component} from 'react';
import {PagingListView, OrderRequest, Tabs} from 'common/components';
import {View, Text, Container, Spinner, Header, Body, Title, Tab} from 'native-base';
import {getDaoState, getNavigationProps, resetSubscriptionAction} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {connect,ReduxRouter, Route} from 'custom-redux';

const DRIVER_ORDER_SUMMARY_DEFAULT_OPTIONS = {
  columnsToSort: [{ name: 'from', direction: 'asc' }, { name: 'orderId', direction: 'asc' }],
  reportId: 'driverOrderSummary',
  driverId: '@userId',
  userId: undefined
};

const CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS = {
  columnsToSort: [{ name: 'from', direction: 'asc' }, { name: 'orderId', direction: 'asc' }],
  reportId: 'customerOrderSummary',
  userId: '@userId',
  driverId: undefined
};

const Paging = () => <Spinner />;
const NoItems = () => <Text empty>{isCustomer ? 'You have no posted jobs' : 'You have no jobs todo'}</Text>;

const RowView = ({item: orderSummary, isLast, isFirst, history, isCustomer, parentPath}) => {
  const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
  let next;
  if (isCustomer){
    next = isOnRoute ? `${parentPath}/CustomerOrderInProgress` : `${parentPath}/CustomerOrderDetail`;
  } else {
    next = isOnRoute ? `${parentPath}/DriverOrderInProgress` : `${parentPath}/DriverOrderDetail`;
  }
  return <OrderRequest history={history} orderSummary={orderSummary} key={orderSummary.orderId} next={next} isLast={isLast} isFirst={isFirst}/>;
};

const getOptions =  (isCustomer, isCompleted) => ({
  ...(isCustomer ? CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS : DRIVER_ORDER_SUMMARY_DEFAULT_OPTIONS),
  isCompleted
});

const OrderListings =  ({history, parentPath, isCustomer, isCompleted}) => <View style={{flex: 1}}>
  <PagingListView
    daoName='orderSummaryDao'
    dataPath={['orders']}
    rowView={RowView}
    history={history}
    parentPath={parentPath}
    options={getOptions(isCustomer, isCompleted)}
    paginationWaitingView={Paging}
    emptyView={NoItems}
    pageSize={10}
    headerView={() => null}
  />
</View>;

class DriverOrders  extends Component{
  constructor(props){
    super(props);
    this.onChangeTab = this.onChangeTab.bind(this);
  }

  onChangeTab (newIsCustomer){
    const {history, isCustomer, isCompleted, path} = this.props;
    const {location} = history;
    
    if (isCustomer !== newIsCustomer) {
      const newPath = location.pathname.endsWith('Posted') ? `${path}/Accepted` : `${path}/Posted`;
      history.replace({pathname: newPath, transition: 'zoom'}, {isCustomer: newIsCustomer, isCompleted});
    }
  }

  render() {
    const {history, isCustomer, defaultOptions, isCompleted, parentPath, path, height} = this.props;
    return <Container>
      <Header hasTabs>
        <Body><Title>{'My Jobs' + (isCompleted ? ' (Completed)' : '')}</Title></Body>
      </Header>
      <Tabs initialPage={history.location.pathname.endsWith('Posted') ? 1 : 0} {...shotgun.tabsStyle} onChangeTab={({i}) => this.onChangeTab(i == 1)}>
        <Tab heading={'Accepted'}/>
        <Tab heading={'Posted'}/>
      </Tabs>
      <ReduxRouter height={height - 150} defaultRoute={`${path}/Accepted`} {...{history, isCustomer, defaultOptions, isCompleted, parentPath, path} } >
        <Route path={`${path}/Accepted`} component={OrderListings} isCustomer={false}/>
        <Route path={`${path}/Posted`} component={OrderListings}  isCustomer={true}/>
      </ReduxRouter>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const navigationProps = getNavigationProps(initialProps);

  return {
    ...initialProps,
    ...navigationProps,
    vehicle: getDaoState(state, ['vehicle'], 'vehicleDao'),
    position: getDaoState(state, ['position'], 'userDao')
  };
};

export default connect(mapStateToProps )(DriverOrders);
