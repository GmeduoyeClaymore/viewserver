import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {PagingListView, OrderRequest, Tabs} from 'common/components';
import {View, Text, Container, Spinner, Header, Body, Title, Tab} from 'native-base';
import {getDaoState, getNavigationProps, resetSubscriptionAction} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';

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


class DriverOrders  extends Component{
  constructor(props){
    super(props);
  }

  componentWillMount(){
    const {resetOrders} = this.props;
    if (resetOrders){
      resetOrders();
    }
  }

  render() {
    const {history, isCustomer, defaultOptions, isCompleted} = this.props;
    const {location} = history;

    const Paging = () => <Spinner />;
    const NoItems = () => <Text empty>{isCustomer ? 'You have no posted jobs' : 'You have no jobs todo'}</Text>;

    const RowView = ({item: orderSummary, isLast, isFirst}) => {
      const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
      const next = isOnRoute ? '/Driver/DriverOrderInProgress' : '/Driver/DriverOrderDetail';
      return <OrderRequest history={history} orderSummary={orderSummary} key={orderSummary.orderId} next={next} isLast={isLast} isFirst={isFirst}/>;
    };

    const onChangeTab = (newIsCustomer) => {
      if (isCustomer !== newIsCustomer) {
        history.replace(location.pathname, {isCustomer: newIsCustomer, isCompleted});
      }
    };

    return <Container>
      <Header hasTabs>
        <Body><Title>{'My Jobs' + (isCompleted ? ' (Completed)' : '')}</Title></Body>
      </Header>
      <Tabs initialPage={isCustomer ? 1 : 0} {...shotgun.tabsStyle} onChangeTab={({i}) => onChangeTab(i == 1)}>
        <Tab heading={'Accepted'}/>
        <Tab heading={'Posted'}/>
      </Tabs>
      <View style={{flex: 1}}>
        <PagingListView
          ref={c => {
            this.pagingListView = c;
          }}
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
  const {isCustomer = false, isCompleted = false} = navigationProps;
  const {dispatch} = initialProps;
  const defaultOptions = {
    ...(isCustomer ? CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS : DRIVER_ORDER_SUMMARY_DEFAULT_OPTIONS),
    isCompleted
  };

  const resetOrders = () => {
    dispatch(resetSubscriptionAction('orderSummaryDao', defaultOptions));
  };

  return {
    ...initialProps,
    ...navigationProps,
    resetOrders,
    defaultOptions,
    isCustomer,
    vehicle: getDaoState(state, ['vehicle'], 'vehicleDao'),
    position: getDaoState(state, ['position'], 'userDao')
  };
};

export default connect(mapStateToProps )(DriverOrders);
