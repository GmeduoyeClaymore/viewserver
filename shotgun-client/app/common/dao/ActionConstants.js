

export const UPDATE_STATUS = 'UPDATE_STATUS';
export const UPDATE_CUSTOMER = 'UPDATE_CUSTOMER';
export const UPDATE_PRODUCT = 'UPDATE_PRODUCT';
export const UPDATE_CART = 'UPDATE_CART';
export const UPDATE_ORDER = 'UPDATE_ORDER';
export const UPDATE_DELIVERY = 'UPDATE_DELIVERY';
export const UPDATE_REGISTRATION_CUSTOMER = 'UPDATE_REGISTRATION_CUSTOMER';
export const UPDATE_REGISTRATION_PAYMENT_CARD = 'UPDATE_REGISTRATION_PAYMENT_CARD';
export const UPDATE_REGISTRATION_DELIVERY_ADDRESS = 'UPDATE_REGISTRATION_DELIVERY_ADDRESS';

const getOr = (val, suffix = '') =>{
  return val ? val.toUpperCase() + suffix : '';
};
export const REGISTER_DAO_ACTION = (dao) => `REGISTER_DAO_${getOr(dao)}`;
export const UNREGISTER_DAO_ACTION = (dao) => `UNREGISTER_DAO_${getOr(dao)}`;
export const UPDATE_STATE = (dao) => `UPDATE_STATE_${getOr(dao)}`;
export const UPDATE_OPTIONS = (dao) => `UPDATE_OPTIONS_${getOr(dao)}`;
export const INVOKE_DAO_COMMAND = (dao, name) => `INVOKE_COMMAND_${getOr(dao, name ? '_' : '')}${getOr(name)}`;
export const UPDATE_COMMAND_STATUS = (dao, command) => `UPDATE_COMMAND_STATUS_${getOr(dao, command ? '_' : '')}${getOr(command)}`;
export const UPDATE_COMPONENT_STATE = (stateKey) => `UPDATE_COMPONENT_STATE_${getOr(stateKey)}`;
export const RESET_ALL_COMPONENT_STATE = 'RESET_ALL_COMPONENT_STATE';
