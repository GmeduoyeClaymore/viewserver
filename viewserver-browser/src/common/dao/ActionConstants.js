
const getOr = (val, suffix = '') =>{
  return val ? val.toUpperCase() + suffix : '';
};
export const REGISTER_DAO_ACTION = (dao) => `REGISTER_DAO_${getOr(dao)}`;
export const UNREGISTER_DAO_ACTION = (dao) => `UNREGISTER_DAO_${getOr(dao)}`;
export const UPDATE_STATE = (dao) => `UPDATE_STATE_${getOr(dao)}`;
export const UPDATE_OPTIONS = (dao) => `UPDATE_OPTIONS_${getOr(dao)}`;
export const UPDATE_TOTAL_SIZE = (dao) => `UPDATE_TOTAL_SIZE_${getOr(dao)}`;
export const INVOKE_DAO_COMMAND = (dao, name) => `INVOKE_COMMAND_${getOr(dao, name ? '_' : '')}${getOr(name)}`;
export const UPDATE_COMMAND_STATUS = (dao, command) => `UPDATE_COMMAND_STATUS_${getOr(dao, command ? '_' : '')}${getOr(command)}`;
