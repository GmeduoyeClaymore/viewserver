import * as TableNames from 'common/constants/TableNames';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {forEach} from 'lodash';
import PrincipalService from 'common/services/PrincipalService';
import moment from 'moment';

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
    this.getPositionAsPromise = this.getPositionAsPromise.bind(this);
    this.position = undefined;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 1,
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
      user: dataSink.rows[0],
      position: this.position
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

  getPositionAsPromise(){
    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, {enableHighAccuracy: true});
    });
  }

  extendDao(dao){
    dao.addOrUpdateUser = async ({user}) => {
      let userRowEvent;
      const {dataSink, subscriptionStrategy} = dao;
      const schema = await dataSink.waitForSchema();
      const {userId} = dao.options;
      const lastModified = moment().format('x');
      Logger.info(`Adding user schema is ${JSON.stringify(schema)}`);

      //TODO - tidy this up using lodash or similar
      const userObject = {};
      forEach(schema, value => {
        const field = value.name;
        userObject[field] = user[field];
      });

      userObject.lastModified = lastModified;

      if (userObject.userId == undefined) {
        userObject.userId = userId;
      }

      if (!dataSink.rows.length){
        Logger.info(`Adding user ${JSON.stringify(userObject)}`);
        userObject.created = lastModified;
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
    dao.getCurrentPosition = async () =>{
      const position = await this.getPositionAsPromise().timeoutWithError(3000, 'Unable to get current position after 3 seconds');
      Logger.info(`Got user position as ${JSON.stringify(position)}`);
      this.position = position.coords;
      dao.subject.next(this.mapDomainEvent(null, dao.dataSink));
    };
  }
}

