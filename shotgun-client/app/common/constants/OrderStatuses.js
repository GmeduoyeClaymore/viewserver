
export const OrderStatuses = {
  PLACED: 'PLACED',
  ACCEPTED: 'ACCEPTED',
  INPROGRESS: 'INPROGRESS',
  COMPLETED: 'COMPLETED',
  COMPLETEDBYDRIVER: 'COMPLETEDBYDRIVER',
  COMPLETEDBYCUSTOMER: 'COMPLETEDBYCUSTOMER',
  CANCELLED: 'CANCELLED'
};

export const getDeliveryFriendlyOrderStatusName = (summary) => {
  switch (summary.status){
  case OrderStatuses.PLACED:
    return 'Awaiting Driver';
  case OrderStatuses.ACCEPTED:
    return 'Driver assigned';
  case OrderStatuses.INPROGRESS:
    return 'On it\'s ways';
  case OrderStatuses.COMPLETED:
    return 'Complete';
  case OrderStatuses.CANCELLED:
    return 'Cancelled';
  default:
    return 'Unknown status';
  }
};

export const getRubbishFriendlyOrderStatusName = (summary) => {
  switch (summary.status){
  case OrderStatuses.PLACED:
    return 'Awaiting Driver';
  case OrderStatuses.ACCEPTED:
    return 'Driver assigned';
  case OrderStatuses.INPROGRESS:
    return 'On it\'s ways';
  case OrderStatuses.COMPLETED:
    return 'Complete';
  case OrderStatuses.CANCELLED:
    return 'Cancelled';
  default:
    return 'Unknown status';
  }
};

export const getProductBasedFriendlyOrderStatusName = (summary) => {
  const {product = {}} = summary;
  switch (summary.status){
  case OrderStatuses.PLACED:
    return 'Awaiting ' + product.name;
  case OrderStatuses.ACCEPTED:
    return product.name + ' assigned';
  case OrderStatuses.INPROGRESS:
    return product.name + ' on their way';
  case OrderStatuses.COMPLETED:
    return 'Complete';
  case OrderStatuses.CANCELLED:
    return 'Cancelled';
  default:
    return 'Unknown status';
  }
};

export const getPossibleStatuses = ({
  orderSummaryStatus,
  iAmTheCustomer,
  doubleComplete}) => {
  invariant(orderSummary, 'Order summary must be defined');

  if (orderSummaryStatus == OrderStatuses.PLACED){
    if (iAmTheCustomer){
      return [OrderStatuses.CANCELLED];
    }
    return [OrderStatuses.ACCEPTED];
  }

  if (orderSummaryStatus == OrderStatuses.ACCEPTED){
    if (iAmTheCustomer){
      return [OrderStatuses.PLACED, OrderStatuses.CANCELLED];
    }
    return [ OrderStatuses.INPROGRESS];
  }

  if (orderSummaryStatus == OrderStatuses.INPROGRESS){
    if (iAmTheCustomer){
      return doubleComplete ? [OrderStatuses.COMPLETEDBYCUSTOMER] : [];
    }
    return [doubleComplete ? OrderStatuses.COMPLETEDBYDRIVER : OrderStatuses.COMPLETED];
  }

  if (orderSummaryStatus == OrderStatuses.COMPLETEDBYCUSTOMER){
    if (iAmTheCustomer){
      return [];
    }
    return doubleComplete ? [OrderStatuses.COMPLETED] : [];
  }

  if (orderSummaryStatus == OrderStatuses.COMPLETEDBYDRIVER){
    if (iAmTheCustomer){
      return doubleComplete ? [OrderStatuses.COMPLETED] : [];
    }
    return [];
  }
};

