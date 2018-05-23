import ReportSubscriptionStrategy from 'common/subscriptionStrategies/ReportSubscriptionStrategy';
import Logger from 'common/Logger';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {hasAnyOptionChanged} from 'common/dao';
export default class UserDaoContext{
  constructor(client, name = 'userDao', options = {}) {
    this.client = client;
    this.options = options;
    this.getPositionAsPromise = this.getPositionAsPromise.bind(this);
    this.watchPositionOnSuccess = this.watchPositionOnSuccess.bind(this);
    this.watchPositionOnError = this.watchPositionOnError.bind(this);
    this.locationOptions = {enableHighAccuracy: true, timeout: 20000, maximumAge: 1000, distanceFilter: 10};
    this._name = name;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 1,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: undefined, //Filtering
      flags: undefined
    };
  }

  getReportContext({userId = '@userId'}){
    return {
      reportId: 'userReport',
      dimensions: {
        dimension_userId: [userId]
      }
    };
  }

  get name(){
    return  this._name;
  }

  createDataSink = () => {
    return new RxDataSink(this._name);
  }

  mapDomainEvent(dataSink){
    return {
      user: this.mapUser(dataSink.rows[0])
    };
  }

  mapUser(user){
    if (!user){
      return user;
    }
    //TODO - for some reason the Viewserver sends through null dates as zeros...
    //TODO - hack to deal with weird VS issue with null json coming through as a 1
    return {...user, status: user.userStatus, vehicle: user.vehicle == 1 ? undefined : user.vehicle};
  }

  createSubscriptionStrategy(options, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext(options), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || hasAnyOptionChanged(previousOptions, newOptions, ['userId']);
  }

  transformOptions(options){
    return {...options};
  }

  getPositionAsPromise(){
    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, {enableHighAccuracy: true});
    });
  }

  async watchPositionOnSuccess(position){
    const {coords} = position;
    const {latitude, longitude} = coords;
    await this.client.invokeJSONCommand('userController', 'setLocation', {latitude, longitude});
  }

  watchPositionOnError(e){
    Logger.warning(e);
  }

  extendDao(dao){
    dao.watchPosition = async () => {
      const position = await this.getPositionAsPromise().timeoutWithError(3000, 'Unable to get current position after 3 seconds');
      Logger.info(`Got user position as ${JSON.stringify(position)}`);
      await this.watchPositionOnSuccess(position);

      await navigator.geolocation.getCurrentPosition((position) => this.watchPositionOnSuccess(position), this.watchPositionOnError, {enableHighAccuracy: true});
      this.watchId = navigator.geolocation.watchPosition((position) => this.watchPositionOnSuccess(position), this.watchPositionOnError, this.locationOptions);
    };

    dao.stopWatchingPosition = async () => {
      navigator.geolocation.clearWatch(this.watchId);
    };

    dao.updateRange = async ({range}) => {
      await this.client.invokeJSONCommand('userController', 'updateRange', {range});
    };

    dao.updateStatus = async ({status, statusMessage}) => {
      await this.client.invokeJSONCommand('userController', 'updateStatus', {status, statusMessage});
    };
  }
}

