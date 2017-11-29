import * as TableNames from 'common/constants/TableNames';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {forEach} from 'lodash';
import PrincipalService from 'common/services/PrincipalService';

const createAddUserEvent = (args) => {
  return {
    type: 0, // ADD
    columnValues: args
  };
};

const createUpdateUserEvent = (args) => {
  return {
    type: 1, // UPDATE
    columnValues: args
  };
};

export default class UserDaoContext{
  constructor(client, options = {}) {
    this.client = client;
    this.options = options;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 20,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      flags: undefined
    };
  }

  get name(){
    return 'userDao';
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      user: dataSink.rows[0]
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, TableNames.USER_TABLE_NAME, dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.userId != newOptions.userId;
  }

  transformOptions(options){
    const {userId} = options;
    if (typeof userId === 'undefined'){
      throw new Error('userId should be defined');
    }
    return {...options, filterExpression: `userId == \"${userId}\"`};
  }

  extendDao(dao){
    dao.addOrUpdateUser = async ({user}) => {
      let userRowEvent;
      const {dataSink, subscriptionStrategy} = dao;
      const schema = await dataSink.waitForSchema();
      const {userId} = dao.options;
      Logger.info(`Adding user schema is ${JSON.stringify(schema)}`);

      //TODO - tidy this up using lodash or similar
      const userObject = {};
      forEach(schema, value => {
        const field = value.name;
        userObject[field] = user[field];
      });

      if (userObject.userId == undefined) {
        userObject.userId = userId;
      }

      if (!dataSink.rows.length){
        Logger.info(`Adding user ${JSON.stringify(userObject)}`);
        userRowEvent = createAddUserEvent(userObject);
      } else {
        Logger.info(`Updating user ${JSON.stringify(userObject)}`);
        userRowEvent = createUpdateUserEvent(userObject);
      }

      const promise = dao.rowEventObservable.filter(ev => ev.row.userId == userId).take(1).timeoutWithError(5000, new Error(`Could not detect modification to user id ${userId} in 5 seconds`)).toPromise();
      await Promise.all([promise, subscriptionStrategy.editTable([userRowEvent])]);
      Logger.info('Add user promise resolved');

      await PrincipalService.setUserIdOnDevice(userObject.userId);
      return userObject.userId;
    };
  }
}

