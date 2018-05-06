import {getDaoState} from './DaoStateUtils';
export const findOrderSummaryFromDao = (state, orderId, daoName) => {
  const orderSummaries = getDaoState(state, ['orders'], daoName) || [];
  return orderSummaries.find(o => o.orderId == orderId);
};
  
