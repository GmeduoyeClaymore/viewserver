
export const OrderStatuses = {
  PLACED: 'PLACED',
  ACCEPTED: 'ACCEPTED',
  PICKEDUP: 'PICKEDUP',
  COMPLETED: 'COMPLETED',
};

export const getFriendlyOrderStatusName = (orderStatus) => {
  switch (orderStatus){
  case OrderStatuses.PLACED:
    return 'Pending';
  case OrderStatuses.ACCEPTED:
    return 'Driver assigned';
  case OrderStatuses.PICKEDUP:
    return 'On it\'s ways';
  case OrderStatuses.COMPLETED:
    return 'Complete';
  default:
    return 'Unknown status';
  }
};
