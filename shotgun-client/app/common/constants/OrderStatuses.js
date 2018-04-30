import invariant from 'invariant';

export const OrderStatuses = {
  PLACED: 'PLACED',
  ACCEPTED: 'ACCEPTED',
  INPROGRESS: 'INPROGRESS',
  COMPLETED: 'COMPLETED',
  COMPLETEDBYPARTNER: 'COMPLETEDBYPARTNER',
  COMPLETEDBYCUSTOMER: 'COMPLETEDBYCUSTOMER',
  CANCELLED: 'CANCELLED'
};

export const getDeliveryFriendlyOrderStatusName = (summary) => {
  switch (summary.status){
  case OrderStatuses.PLACED:
    return 'Awaiting Partner';
  case OrderStatuses.ACCEPTED:
    return 'Partner assigned';
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
    return 'Awaiting Partner';
  case OrderStatuses.ACCEPTED:
    return 'Partner assigned';
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
  invariant(orderSummaryStatus, 'orderSummaryStatus must be defined');

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
    return [doubleComplete ? OrderStatuses.COMPLETEDBYPARTNER : OrderStatuses.COMPLETED];
  }

  if (orderSummaryStatus == OrderStatuses.COMPLETEDBYCUSTOMER){
    if (iAmTheCustomer){
      return [];
    }
    return doubleComplete ? [OrderStatuses.COMPLETED] : [];
  }

  if (orderSummaryStatus == OrderStatuses.COMPLETEDBYPARTNER){
    if (iAmTheCustomer){
      return doubleComplete ? [OrderStatuses.COMPLETED] : [];
    }
    return [];
  }
  if (orderSummaryStatus == OrderStatuses.COMPLETED){
    return [];
  }
  throw new Error('Unrecognized order summary status ' + orderSummaryStatus);
};

