class OrderListings extends Component{
  getOptions = (isCustomer, isCompleted) => ({
    ...(isCustomer ? CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS : PARTNER_ORDER_SUMMARY_DEFAULT_OPTIONS),
    isCompleted
  });

  render(){
    const {history, ordersRoot, isCustomer, isCompleted} = this.props;
    return <View style={{flex: 1}}>
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
  }
}

export default OrderListings;
