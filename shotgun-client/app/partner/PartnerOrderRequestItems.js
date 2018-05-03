import React, {Component} from 'react';
import {PagingListView, OrderRequest} from 'common/components';
import {Text, Spinner} from 'native-base';
import OrderRequestDao from 'partner/dao/OrderRequestDao';

class PartnerOrderRequestItems extends Component {
  NoItems = () => <Text empty>No jobs available</Text>;
  RowView = ({item: order, isLast, isFirst, history, parentPath}) =>
    <OrderRequest history={history} order={order} key={order.orderId} isLast={isLast} isFirst={isFirst} next={`${parentPath}/PartnerOrderRequestDetail`}/>;

  getDefaultOptions = (contentType, contentTypeOptions) => {
    return {
      ...OrderRequestDao.PARTNER_ORDER_REQUEST_DEFAULT_OPTIONS,
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

export default PartnerOrderRequestItems;
