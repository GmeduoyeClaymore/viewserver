import React, {Component} from 'react';
import {connect, ReduxRouter, Route} from 'custom-redux';
import {PagingListView, Tabs, OrderRequest} from 'common/components';
import {View, Container, Spinner, Header, Body, Title, Tab, Text} from 'native-base';
import {isAnyLoading, getNavigationProps, resetSubscriptionAction} from 'common/dao';
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

  onChangeTab(){
    const {history, path} = this.props;
    const {location} = history;
    const newPath = location.pathname.endsWith('Complete') ?   'Live' : 'Complete';
    history.replace({pathname: newPath});
  }


  render(){
    const {isCompleted, history, path, height, parentPath} = this.props;
    const {onChangeTab} = this;
  
    return <Container>
      <Header hasTabs>
        <Body><Title>My Jobs</Title></Body>
      </Header>
      <Tabs initialPage={history.location.pathname.endsWith('Complete')  ? 1 : 0} {...shotgun.tabsStyle} onChangeTab={({i}) => onChangeTab()}>
        <Tab heading="Live Jobs"/>
        <Tab heading="Complete"/>
      </Tabs>
      <ReduxRouter  name="CustomerOrdersRouter"  history={history} parentPath={parentPath}  height={height - 150} path={path} defaultRoute={'Live'}>
        <Route path={'Live'} parentPath={parentPath}  isCompleted={false} component={OrderItems}/>
        <Route path={'Complete'} parentPath={parentPath}  isCompleted={true} component={OrderItems}/>
      </ReduxRouter>
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
    busy: isAnyLoading(state, ['orderSummaryDao']),
  };
};

export default connect(
  mapStateToProps
)(CustomerOrders);
