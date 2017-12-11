import {invokeDaoCommand, getDaoOptions} from 'common/dao';
import {register} from 'common/actions/CommonActions';
import GenericOperatorDaoContext from 'dao/GenericOperatorDaoContext';



export const registerDataDaos = (continueWith) => {
  return async (dispatch, getState) => {
    const state = getState();
    if (!state.getIn(['dao', 'dataSourceDao'])){
      register(dispatch, new GenericOperatorDaoContext("dataSourcesDao", {operatorName : "/datasources"}));
      register(dispatch, new GenericOperatorDaoContext("reportsDao", {operatorName : "/report_registry"}));
      register(dispatch, new GenericOperatorDaoContext("sessionsDao", {operatorName : "/sessions"}));
      register(dispatch, new GenericOperatorDaoContext("operatorListDao", {}));
      //register(dispatch, new GenericOperatorDaoContext("operatorContentsDao", {}));
    } 
    if(continueWith){
      continueWith();
    }
  };
};
