import shotgun from 'native-base-theme/variables/shotgun';

export const NegotiationStatuses = {
  RESPONDED: 'RESPONDED',
  ACCEPTED: 'ACCEPTED',
  DECLINED: 'DECLINED',
  REJECTED: 'REJECTED'
};

export const getPartnerFriendlyNegotiationStatusName = (order) => {
  switch (order.responseStatus){
  case NegotiationStatuses.RESPONDED:
    return 'Awaiting customer response';
  case NegotiationStatuses.DECLINED:
    return 'Customer declined your offer';
  case NegotiationStatuses.REJECTED:
    return 'Customer accepted another offer';
  default:
    return 'Unknown status - ' + order.negotiatedOrderStatus;
  }
};

export const getPartnerNegotiationColor =  (order) => {
  switch (order.responseStatus){
  case NegotiationStatuses.RESPONDED:
    return shotgun.brandWarning;
  case NegotiationStatuses.DECLINED:
    return shotgun.brandDanger;
  case NegotiationStatuses.REJECTED:
    return shotgun.brandDanger;
  default:
    return undefined;
  }
};
