
export const OrderStatuses = {
  PLACED: 'PLACED',
  ACCEPTED: 'ACCEPTED',
  PICKEDUP: 'PICKEDUP',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED'
};

export const getFriendlyOrderStatusName = (orderStatus) => {
  switch (orderStatus){
  case OrderStatuses.PLACED:
    return 'Awaiting Driver';
  case OrderStatuses.ACCEPTED:
    return 'Driver assigned';
  case OrderStatuses.PICKEDUP:
    return 'On it\'s ways';
  case OrderStatuses.COMPLETED:
    return 'Complete';
  case OrderStatuses.CANCELLED:
    return 'Cancelled';
  default:
    return 'Unknown status';
  }
};
