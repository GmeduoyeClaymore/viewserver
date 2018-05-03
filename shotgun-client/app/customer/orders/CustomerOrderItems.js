import React, {Component} from 'react';
import {PagingListView, OrderListItem} from 'common/components';
import {Spinner, Text} from 'native-base';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';

class OrderItems extends Component {
  NoItems = () => <Text empty>No jobs to display</Text>;

  RowView = ({item: order, isLast, isFirst, history, parentPath}) => {
    const isOnRoute = order.orderStatus == OrderStatuses.INPROGRESS;
    const next = isOnRoute ? `${parentPath}/CustomerOrderInProgress` : `${parentPath}/CustomerOrderDetail`;
    return <OrderListItem history={history} order={order} key={order.orderId} next={next} isLast={isLast} isFirst={isFirst}/>;
  };

  getDefaultOptions = (isCompleted) => ({
    ...OrderSummaryDao.CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS,
    isCompleted
  });

  render() {
    const {history, parentPath, isCompleted} = this.props;

    return <PagingListView
      daoName='orderSummaryDao'
      dataPath={['orders']}
      history={history}
      rowView={this.RowView}
      options={this.getDefaultOptions(isCompleted)}
      paginationWaitingView={() => <Spinner />}
      emptyView={this.NoItems}
      parentPath={parentPath}
      pageSize={10}
      headerView={() => null}
    />;
  }
}

export default OrderItems;