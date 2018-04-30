import React, {Component} from 'react';
import {PagingListView, OrderRequest} from 'common/components';
import {Text, Spinner} from 'native-base';
import OrderRequestDao from 'driver/dao/OrderRequestDao';

class DriverOrderRequestItems extends Component {
  NoItems = () => <Text empty>No jobs available</Text>;
  RowView = ({item: orderSummary, isLast, isFirst, history, parentPath}) => <OrderRequest history={history}
    orderSummary={orderSummary}
    key={orderSummary.orderId}
    isLast={isLast}
    isFirst={isFirst}
    next={`${parentPath}/DriverOrderRequestDetail`}/>;

  getDefaultOptions = (contentType, contentTypeOptions) => {
    return {
      ...OrderRequestDao.DRIVER_ORDER_REQUEST_DEFAULT_OPTIONS,
      userId: undefined,
      contentType,
      contentTypeOptions
    };
  };

  render() {
    const {history, parentPath, contentType, contentTypeOptions} = this.props;

    return <PagingListView
      daoName='orderRequestDao'
      parentPath={parentPath}
      history={history}
      dataPath={['driver', 'orders']}
      rowView={this.RowView}
      options={this.getDefaultOptions(contentType, contentTypeOptions)}
      paginationWaitingView={() => <Spinner/>}
      emptyView={this.NoItems}
      pageSize={10}
      headerView={() => null}/>;
  }
}

export default DriverOrderRequestItems;
