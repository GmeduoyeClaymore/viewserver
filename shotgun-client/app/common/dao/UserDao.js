import * as TableNames from 'common/constants/TableNames';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';
import RxDataSink from 'common/dataSinks/RxDataSink';

export default class UserDaoContext{
  constructor(client, options = {}) {
    this.client = client;
    this.options = options;
    this.getPositionAsPromise = this.getPositionAsPromise.bind(this);
    this.watchPositionOnSuccess = this.watchPositionOnSuccess.bind(this);
    this.watchPositionOnError = this.watchPositionOnError.bind(this);
    this.position = undefined;
    this.locationOptions = {enableHighAccuracy: true, timeout: 20000, maximumAge: 1000, distanceFilter: 10};
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

  async watchPositionOnSuccess(userId, position){
    const {coords} = position;
    const {latitude, longitude} = coords;
    await this.client.invokeJSONCommand('userController', 'setLocation', {userId, latitude, longitude});
  }

  watchPositionOnError(e){
    Logger.warning(e);
  }

  extendDao(dao){
    dao.getCurrentPosition = async () =>{
      const position = await this.getPositionAsPromise().timeoutWithError(3000, 'Unable to get current position after 3 seconds');
      Logger.info(`Got user position as ${JSON.stringify(position)}`);
      this.position = position.coords;
      dao.subject.next(this.mapDomainEvent(null, dao.dataSink));
    };
    dao.watchPosition = async () => {
      const {userId} = dao.options;
      await navigator.geolocation.getCurrentPosition((position) => this.watchPositionOnSuccess(userId, position), this.watchPositionOnError, {enableHighAccuracy: true});
      this.watchId = navigator.geolocation.watchPosition((position) => this.watchPositionOnSuccess(userId, position), this.watchPositionOnError, this.locationOptions);
    };
    dao.stopWatchingPosition = async () => {
      navigator.geolocation.clearWatch(this.watchId);
    };
  }
}

