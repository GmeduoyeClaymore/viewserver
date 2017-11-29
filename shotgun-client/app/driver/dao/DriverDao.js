import * as TableNames from 'common/constants/TableNames';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {forEach} from 'lodash';
import PrincipalService from 'common/services/PrincipalService';

export default class DriverDaoContext{
  constructor(client, userDao, vehicleDao, options = {}) {
    this.client = client;
    this.userDao = userDao;
    this.vehicleDao = vehicleDao;
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
    return 'driverDao';
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(){
    return {
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
    dao.addOrUpdateDriver = async ({driver, vehicle}) => {
      const {dataSink} = dao;
      const schema = await dataSink.waitForSchema();
      const {userId} = dao.options;
      Logger.info(`Adding driver schema is ${JSON.stringify(schema)}`);

      //TODO - tidy this up using lodash or similar
      const user = {};
      forEach(schema, value => {
        const field = value.name;
        user[field] = driver[field];
      });

      if (user.userId == undefined) {
        user.userId = userId;
      }

      await this.userDao.addOrUpdateUser({user});
      await this.vehicleDao.addOrUpdateVehicle({userId: user.userId, vehicle});

      await PrincipalService.setUserIdOnDevice(user.userId);

      return user.userId;
    };
  }
}

