import React, {Component} from 'react';
import {PagingListView, OrderListItem} from 'common/components';
import {Text, Spinner} from 'native-base';


class PartnerMyOrdersListView extends Component{
  NoItems = ({emptyCaption}) => <Text empty>{emptyCaption}</Text>;

  RowView = ({item: order, isLast, isFirst, history, isCustomer, ordersRoot, orderStatusResolver}) => {
    let next;
    if (isCustomer){
      next = `${ordersRoot}/CustomerOrderDetail`;
    } else {
      next = `${ordersRoot}/PartnerOrderDetail`;
    }
    return <OrderListItem history={history} order={order} key={order.orderId} next={next} isLast={isLast} isFirst={isFirst} orderStatusResolver={orderStatusResolver}/>;
  };

  render(){
    const {history, ordersRoot, isCustomer, options, daoName, emptyCaption, orderStatusResolver} = this.props;

    return <PagingListView
      daoName={daoName}
      dataPath={['orders']}
      rowView={this.RowView}
      history={history}
      ordersRoot={ordersRoot}
      isCustomer={isCustomer}
      emptyCaption={emptyCaption}
      orderStatusResolver={orderStatusResolver}
      options={options}
      paginationWaitingView={() => <Spinner />}
      emptyView={this.NoItems}
      pageSize={10}
      headerView={() => null}
    />;
  }
}

export default PartnerMyOrdersListView;
