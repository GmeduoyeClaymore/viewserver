import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {PagingListView, OrderRequest, Tabs} from 'common/components';
import { withRouter } from 'react-router';
import {View, Text, Container, Spinner, Header, Body, Title, Tab} from 'native-base';
import {getDaoState, getNavigationProps, resetSubscriptionAction} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';

const DRIVER_ORDER_SUMMARY_DEFAULT_OPTIONS = {
  columnsToSort: [{ name: 'from', direction: 'asc' }, { name: 'orderId', direction: 'asc' }],
  reportId: 'driverOrderSummary'
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
    const {history, isCompleted, defaultOptions} = this.props;
    const {location} = history;

    const Paging = () => <Spinner />;
    const NoItems = () => <Text empty>{isCompleted ? 'You have no completed jobs' : 'You have no live jobs'}</Text>;

    const RowView = ({item: orderSummary, isLast, isFirst}) => {
      const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
      const next = isOnRoute ? '/Driver/DriverOrderInProgress' : '/Driver/DriverOrderDetail';
      return <OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} next={next} isLast={isLast} isFirst={isFirst}/>;
    };

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
  const {isCompleted = false} = navigationProps;
  const {dispatch} = initialProps;
  const defaultOptions = {
    ...DRIVER_ORDER_SUMMARY_DEFAULT_OPTIONS,
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
    vehicle: getDaoState(state, ['vehicle'], 'vehicleDao'),
    position: getDaoState(state, ['position'], 'userDao')
  };
};

export default withRouter(connect(mapStateToProps )(DriverOrders));
