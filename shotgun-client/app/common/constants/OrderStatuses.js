import shotgun from 'native-base-theme/variables/shotgun';

export const OrderStatuses = {
  PLACED: 'PLACED',
  ACCEPTED: 'ACCEPTED',
  INPROGRESS: 'INPROGRESS',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  COMPLETEDBYPARTNER: 'COMPLETEDBYPARTNER',
  COMPLETEDBYCUSTOMER: 'COMPLETEDBYCUSTOMER',
  RESPONDED: 'RESPONDED'
};

export const getCustomerFriendlyOrderStatusName = (noun) =>  (order) => {
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
    return 'Paid';
  default:
    return 'Unknown status - ' + order.negotiatedOrderStatus;
  }
};

export const getDeliveryFriendlyOrderStatusName = getCustomerFriendlyOrderStatusName('Driver');
export const getRubbishFriendlyOrderStatusName = getCustomerFriendlyOrderStatusName('Collector');
export const getProductBasedFriendlyOrderStatusName = (order) => getCustomerFriendlyOrderStatusName(order.orderProduct.name)(order);

export const getPartnerFriendlyOrderStatusName =  (order) => {
  switch (order.negotiatedOrderStatus){
  case 'ASSIGNED':
    return 'Job not started';
  case 'STARTED':
    return 'Job In Progress';
  case 'CANCELLED':
    return 'Job Cancelled';
  case 'PARTNERCOMPLETE':
    return 'Awaiting Payment';
  case 'CUSTOMERCOMPLETE':
    return 'Paid';
  default:
    return 'Unknown status - ' + order.negotiatedOrderStatus;
  }
};

export const getPartnerOrderColor =  (order) => {
  switch (order.negotiatedOrderStatus){
  case 'ASSIGNED':
    return undefined;
  case 'STARTED':
    return shotgun.brandSuccess;
  case 'CANCELLED':
    return shotgun.brandDanger;
  case 'PARTNERCOMPLETE':
    return shotgun.brandWarning;
  case 'CUSTOMERCOMPLETE':
    return shotgun.brandSuccess;
  default:
    return undefined;
  }
};

export const getCustomerOrderColor =  (order) => {
  switch (order.negotiatedOrderStatus){
  case 'ASSIGNED':
    return undefined;
  case 'RESPONDED':
    return shotgun.brandWarning;
  case 'STARTED':
    return shotgun.brandSuccess;
  case 'CANCELLED':
    return shotgun.brandDanger;
  case 'PARTNERCOMPLETE':
    return shotgun.brandWarning;
  case 'CUSTOMERCOMPLETE':
    return undefined;
  default:
    return undefined;
  }
};
