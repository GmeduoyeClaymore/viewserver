import React, {Component} from 'react';
import {PagingListView, OrderRequest, Tabs, Icon} from 'common/components';
import {View, Text, Container, Spinner, Header, Body, Title, Tab, Left, Button} from 'native-base';
import {getDaoState, getNavigationProps} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {connect, ReduxRouter, Route} from 'custom-redux';

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
const NoItems = ({isCustomer}) => <Text empty>{isCustomer ? 'You have no posted jobs' : 'You have no jobs todo'}</Text>;

const RowView = ({item: orderSummary, isLast, isFirst, history, isCustomer, ordersRoot}) => {
  const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
  let next;
  if (isCustomer){
    next = isOnRoute ? `${ordersRoot}/CustomerOrderInProgress` : `${ordersRoot}/CustomerOrderDetail`;
  } else {
    next = isOnRoute ? `${ordersRoot}/DriverOrderInProgress` : `${ordersRoot}/DriverOrderDetail`;
  }
  return <OrderRequest history={history} orderSummary={orderSummary} key={orderSummary.orderId} next={next} isLast={isLast} isFirst={isFirst}/>;
};

const getOptions =  (isCustomer, isCompleted) => ({
  ...(isCustomer ? CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS : DRIVER_ORDER_SUMMARY_DEFAULT_OPTIONS),
  isCompleted
});

const OrderListings =  ({history, ordersRoot, isCustomer, isCompleted}) => <View style={{flex: 1}}>
  <PagingListView
    daoName='orderSummaryDao'
    dataPath={['orders']}
    rowView={RowView}
    history={history}
    ordersRoot={ordersRoot}
    isCustomer={isCustomer}
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

  onChangeTab (isCustomer){
    const {history, isCompleted, path, canGoBack} = this.props;
    const newPath = isCustomer ? `${path}/Posted` : `${path}/Accepted`;
    history.replace({pathname: newPath}, {isCompleted, canGoBack});
  }

  render() {
    const {history, isCustomer, defaultOptions, isCompleted, canGoBack, parentPath, path, height, ordersRoot} = this.props;
    return <Container>
      <Header hasTabs  withButton={canGoBack}>
        {canGoBack ? <Left>
          <Button>
            <Icon name='back-arrow' onPress={() => history.goBack()} />
          </Button>
        </Left> : null }
        <Body><Title>{'My Jobs' + (isCompleted ? ' (Completed)' : '')}</Title></Body>
      </Header>
      <Tabs initialPage={isCustomer ? 1 : 0}  page={isCustomer ? 1 : 0} {...shotgun.tabsStyle} onChangeTab={({i}) => this.onChangeTab(i == 1)}>
        <Tab heading={'Accepted'}/>
        <Tab heading={'Posted'}/>
      </Tabs>
      <ReduxRouter  name="DriverOrdersRouter" height={height - 150} defaultRoute={'Accepted'} {...{history, isCustomer, defaultOptions, isCompleted: !!isCompleted, parentPath, ordersRoot, path} } >
        <Route path={'Accepted'} component={OrderListings}/>
        <Route path={'Posted'} component={OrderListings}/>
      </ReduxRouter>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const navigationProps = getNavigationProps(initialProps);
  const {history, path} = initialProps;
  const {location} = history;
  const {pathname} = location;

  return {
    isCustomer: pathname.endsWith(`${path}/Posted`),
    ...initialProps,
    ...navigationProps,
    vehicle: getDaoState(state, ['vehicle'], 'vehicleDao'),
    position: getDaoState(state, ['position'], 'userDao')
  };
};

export default connect(mapStateToProps )(DriverOrders);
