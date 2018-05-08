import React, {Component} from 'react';
import {PagingListView, OrderListItem} from 'common/components';
import {getDeliveryFriendlyOrderStatusName, getRubbishFriendlyOrderStatusName, getProductBasedFriendlyOrderStatusName} from 'common/constants/OrderStatuses';
import {Spinner, Text} from 'native-base';
import * as ContentTypes from 'common/constants/ContentTypes';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';

class CustomerMyOrdersListView extends Component {
  NoItems = () => <Text empty>No jobs to display</Text>;

  RowView = ({item: order, isLast, isFirst, history, parentPath, orderStatusResolver}) => {
    const next = `${parentPath}/CustomerOrderDetail`;
    return <OrderListItem history={history} order={order} key={order.orderId} next={next} isLast={isLast} isFirst={isFirst} orderStatusResolver={orderStatusResolver}/>;
  };

  getDefaultOptions = (isCompleted) => ({
    ...OrderSummaryDao.CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS,
    userId: '@userId',
    isCompleted
  });

  render() {
    const {history, parentPath, isCompleted} = this.props;

    return <PagingListView
      daoName='orderSummaryDao'
      dataPath={['orders']}
      orderStatusResolver={getCustomerBasedOrderStatus}
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

const getCustomerBasedOrderStatus = order => {
  const resources = resourceDictionary.resolve(order.orderContentTypeId);
  return resources.OrderStatusResolver(order);
};

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary().
  property('OrderStatusResolver', getProductBasedFriendlyOrderStatusName).
    delivery(getDeliveryFriendlyOrderStatusName).
    rubbish(getRubbishFriendlyOrderStatusName);
    /*eslint-enable */

export default CustomerMyOrdersListView;
