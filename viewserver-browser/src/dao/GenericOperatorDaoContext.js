import RxDataSink from 'common/dataSinks/RxDataSink';
import OperatorSubscriptionStrategy from 'common/subscriptionStrategies/OperatorSubscriptionStrategy';
import {GetConnectedClientFromLoginDao} from 'common/dao/loginUtils'

export default class GenericOperatorDaoContext{
  constructor(name, options = {}) {
    this.options = options;
    this._name = name;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 50,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      filterExpression: undefined, //Filtering
      flags: undefined,
      ...this.options
    };
  }

  get name(){
    return this._name;
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return dataSink.rows;
  }

  createSubscriptionStrategy(client, options, dataSink){
    return new OperatorSubscriptionStrategy(client, options.operatorName, dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.operatorName != newOptions.operatorName;
  }

  extendDao(dao){
    dao._observable = dao.subject.throttleTime(100)
    const daoContext = this;
    dao.invokeJSONCommand = async ({controller, command, payload}) => {
      const client = await GetConnectedClientFromLoginDao(daoContext);
      return await client.invokeJSONCommand(controller, command, payload);
    };
  }

  transformOptions(options){
    const {operatorName} = options;
    if (!operatorName ){
      throw new Error('operatorName should be defined');
    }
    return {...options};
  }
}


