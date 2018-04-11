import React, {Component} from 'react';
import {connect, ReduxRouter, Route} from 'custom-redux';
import {PagingListView, Tabs, OrderRequest, LoadingScreen} from 'common/components';
import {View, Container, Spinner, Header, Body, Title, Tab, Text} from 'native-base';
import {isAnyLoading, getNavigationProps, resetSubscriptionAction, getDao} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';
const Paging = () => <Spinner />;
const NoItems = () => <Text empty>No orders to display</Text>;
const RowView = ({item: orderSummary, isLast, isFirst, history, parentPath}) => {
  const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
  const next = isOnRoute ? `${parentPath}/CustomerOrderInProgress` : `${parentPath}/CustomerOrderDetail`;
  return <OrderRequest history={history} orderSummary={orderSummary} key={orderSummary.orderId} next={next} isLast={isLast} isFirst={isFirst}/>;
};

const getDefaultOptions = (isCompleted) => ({
  ...CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS,
  isCompleted
});
const OrderItems = ({isCompleted, history, parentPath}) => (
  <View style={{flex: 1}}>
    <PagingListView
      ref={ c => {this.pagingListView = c;}}
      daoName='orderSummaryDao'
      dataPath={['orders']}
      history={history}
      rowView={RowView}
      options={getDefaultOptions(isCompleted)}
      paginationWaitingView={Paging}
      emptyView={NoItems}
      parentPath={parentPath}
      pageSize={10}
      headerView={() => null}
    />
  </View>
);

const CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS = {
  columnsToSort: [{ name: 'from', direction: 'asc' }, { name: 'orderId', direction: 'asc' }],
  reportId: 'customerOrderSummary',
  userId: '@userId',
  driverId: undefined
};

class CustomerOrders extends Component{
  constructor(props){
    super(props);
    this.goToTabNamed = this.goToTabNamed.bind(this);
  }

  goToTabNamed(name){
    const {history, path} = this.props;
    history.replace({pathname: `${path}/${name}`});
  }


  render(){
    const {history, path, height, parentPath, isOrdersDaoRegistered} = this.props;
    const {goToTabNamed} = this;
  
    return <Container>
      <Header hasTabs>
        <Body><Title>My Jobs</Title></Body>
      </Header>
      <Tabs initialPage={history.location.pathname.endsWith('Complete')  ? 1 : 0} page={history.location.pathname.endsWith('Complete')  ? 1 : 0}  {...shotgun.tabsStyle}>
        <Tab heading="Live Jobs" onPress={() => goToTabNamed('Live')}/>
        <Tab heading="Complete" onPress={() => goToTabNamed('Complete')}/>
      </Tabs>
      {isOrdersDaoRegistered ? <ReduxRouter  name="CustomerOrdersRouter" {...this.props}  height={height - 150} path={path} defaultRoute={'Live'}>
        <Route path={'Live'} parentPath={parentPath}  isCompleted={false} component={OrderItems} />
        <Route path={'Complete'} parentPath={parentPath}  isCompleted={true} component={OrderItems} />
      </ReduxRouter> : <LoadingScreen text="Waiting for order data.."/>}
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const navigationProps = getNavigationProps(initialProps);
  const {dispatch} = initialProps;
  const {isCompleted = false} = navigationProps;

  const resetOrders = () => {
    dispatch(resetSubscriptionAction('orderSummaryDao', getDefaultOptions(isCompleted)));
  };
  return {
    ...initialProps,
    resetOrders,
    isCompleted,
    isOrdersDaoRegistered: !!getDao(state, 'orderSummaryDao'),
    busy: isAnyLoading(state, ['orderSummaryDao']),
  };
};

export default connect(
  mapStateToProps
)(CustomerOrders);
