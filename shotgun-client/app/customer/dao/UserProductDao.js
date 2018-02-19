import ReportSubscriptionStrategy from 'common/subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {isEqual} from 'lodash';

export default class UserProductDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    maxDistance: 5000
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...UserProductDaoContext.OPTIONS, ...options};
    this.subscribeOnCreate = false;
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return 'userProductDao';
  }

  getReportContext({selectedProduct, position = {}, maxDistance}){
    const {latitude, longitude} = position;
    const baseReportContext =  {
      reportId: 'usersForProduct',
      parameters: {
        latitude,
        longitude,
        productId: selectedProduct.productId,
        maxDistance
      }
    };
    return baseReportContext;
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(dataSink){
    return {
      users: {
        productUsers: dataSink.rows.map(r => this.mapUser(r)),
      }
    };
  }

  mapUser(user){
    return {...user, latitude: parseFloat(user.latitudeText), longitude: parseFloat(user.longitudeText)};
  }

  createSubscriptionStrategy(options, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext(options), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    //TODO - probably needs to be updated as driver position changed but not sure how often
    return !previousOptions || !isEqual(previousOptions.selectedProduct, newOptions.selectedProduct)  || !isEqual(previousOptions.location, newOptions.location) || previousOptions.maxDistance != newOptions.maxDistance;
  }

  transformOptions(options){
    if (typeof options.position === 'undefined'){
      throw new Error('position should be defined');
    }
    if (typeof options.selectedProduct === 'undefined'){
      throw new Error('selectedProduct  should be defined');
    }
    if (typeof options.maxDistance === 'undefined'){
      throw new Error('maxDistance  should be defined');
    }
    return options;
  }
}
