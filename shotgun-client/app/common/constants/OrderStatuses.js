import invariant from 'invariant';

export const OrderStatuses = {
  PLACED: 'PLACED',
  ACCEPTED: 'ACCEPTED',
  INPROGRESS: 'INPROGRESS',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  COMPLETEDBYPARTNER: 'COMPLETEDBYPARTNER',
  COMPLETEDBYCUSTOMER: 'COMPLETEDBYCUSTOMER',
};

export const getFriendlyOrderStatusName = (noun) =>  (order) => {
  switch (order.negotiatedOrderStatus){
  case 'REQUESTED':
    return `Awaiting ${noun}`;
  case 'RESPONDED':
    return order.responses.length > 1 ?  `${order.responses.length} ${noun} responses` : `${noun} Responded`;
  case 'ASSIGNED':
    return `${noun} Assigned`;
  case 'STARTED':
    return 'Job In Progress';
  case 'CANCELLED':
    return 'Job Cancelled';
  case 'PARTNERCOMPLETE':
    return `${noun} Finished`;
  case 'CUSTOMERCOMPLETE':
    return 'Complete';
  default:
    return 'Unknown status - ' + order.negotiatedOrderStatus;
  }
};

export const getDeliveryFriendlyOrderStatusName = getFriendlyOrderStatusName('Driver');
export const getRubbishFriendlyOrderStatusName = getFriendlyOrderStatusName('Collector');
export const getProductBasedFriendlyOrderStatusName = (order) => getFriendlyOrderStatusName(order.orderProduct.name)(order);
