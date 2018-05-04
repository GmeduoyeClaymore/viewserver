import React, {Component} from 'react';
import {PagingListView, OrderListItem} from 'common/components';
import {Text, Spinner} from 'native-base';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';

class PartnerMyOrdersListView extends Component{
  NoItems = ({isCustomer}) => <Text empty>{isCustomer ? 'You have no posted jobs' : 'You have no jobs to do'}</Text>;

  RowView = ({item: order, isLast, isFirst, history, isCustomer, ordersRoot}) => {
    const isOnRoute = order.orderStatus == OrderStatuses.INPROGRESS;
    let next;
    if (isCustomer){
      next = isOnRoute ? `${ordersRoot}/CustomerOrderInProgress` : `${ordersRoot}/CustomerOrderDetail`;
    } else {
      next = isOnRoute ? `${ordersRoot}/PartnerOrderInProgress` : `${ordersRoot}/PartnerMyOrderDetail`;
    }
    return <OrderListItem history={history} order={order} key={order.orderId} next={next} isLast={isLast} isFirst={isFirst}/>;
  };

  getOptions = (isCustomer, isCompleted) => ({
    ...(isCustomer ? OrderSummaryDao.CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS : {}),
    isCompleted
  });

  render(){
    const {history, ordersRoot, isCustomer, isCompleted} = this.props;

    return <PagingListView
      daoName={isCustomer ? 'orderSummaryDao' : 'partnerOrderResponseDao'}
      dataPath={['orders']}
      rowView={this.RowView}
      history={history}
      ordersRoot={ordersRoot}
      isCustomer={isCustomer}
      options={this.getOptions(isCustomer, isCompleted)}
      paginationWaitingView={() => <Spinner />}
      emptyView={this.NoItems}
      pageSize={10}
      headerView={() => null}
    />;
  }
}

export default PartnerMyOrdersListView;
