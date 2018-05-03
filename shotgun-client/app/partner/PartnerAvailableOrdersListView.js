import React, {Component} from 'react';
import {PagingListView, OrderListItem} from 'common/components';
import {Text, Spinner} from 'native-base';
import OrderRequestDao from 'partner/dao/OrderRequestDao';

export default class PartnerAvailableOrdersListView extends Component {
  NoItems = () => <Text empty>No jobs available</Text>;
  RowView = ({item: order, isLast, isFirst, history, parentPath}) =>
    <OrderListItem history={history} order={order} key={order.orderId} isLast={isLast} isFirst={isFirst} next={`${parentPath}/PartnerAvailableOrderDetail`}/>;

  getDefaultOptions = (contentType, contentTypeOptions) => {
    return {
      ...OrderRequestDao.PARTNER_AVAILABLE_ORDERS_DEFAULT_OPTIONS,
      userId: undefined,
      contentType,
      contentTypeOptions,
      showOutOfRange: true
    };
  };

  render() {
    const {history, parentPath, contentType, contentTypeOptions} = this.props;

    return <PagingListView
      daoName='orderRequestDao'
      parentPath={parentPath}
      history={history}
      dataPath={['orders']}
      rowView={this.RowView}
      options={this.getDefaultOptions(contentType, contentTypeOptions)}
      paginationWaitingView={() => <Spinner/>}
      emptyView={this.NoItems}
      pageSize={10}
      headerView={() => null}/>;
  }
}
