
export const OrderStatuses = {
  PLACED: 'PLACED',
  ACCEPTED: 'ACCEPTED',
  PICKEDUP: 'PICKEDUP',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED'
};

export const getDeliveryFriendlyOrderStatusName = (summary) => {
  switch (summary.status){
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

export const getRubbishFriendlyOrderStatusName = (summary) => {
  switch (summary.status){
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

export const getProductBasedFriendlyOrderStatusName = (summary) => {
  const {product = {}} = summary;
  switch (summary.status){
  case OrderStatuses.PLACED:
    return 'Awaiting ' + product.name;
  case OrderStatuses.ACCEPTED:
    return product.name + ' assigned';
  case OrderStatuses.PICKEDUP:
    return product.name + 'On their way';
  case OrderStatuses.COMPLETED:
    return 'Complete';
  case OrderStatuses.CANCELLED:
    return 'Cancelled';
  default:
    return 'Unknown status';
  }
};

