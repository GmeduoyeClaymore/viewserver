import ReportSubscriptionStrategy from 'common/subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {hasAnyOptionChanged} from 'common/dao';


export default class UserRelationshipDaoContext{
  static DEFAULT_POSITION = {
    latitude: 0,
    longitude: 0
  }

  static OPTIONS = {
    offset: 0,
    limit: 100,
    filterMode: 20,
    showUnrelated: true,
    showOutOfRange: true,
    position: UserRelationshipDaoContext.DEFAULT_POSITION,
    maxDistance: 0
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...UserRelationshipDaoContext.OPTIONS, ...options};
    this.subscribeOnCreate = false;
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return 'userRelationshipDao';
  }

  getReportContext({reportId, selectedProduct = {}, position = UserRelationshipDaoContext.DEFAULT_POSITION, maxDistance = 0, showUnrelated = true, showOutOfRange = true}){
    const {latitude, longitude} = position;
    const baseReportContext =  {
      reportId,
      parameters: {
        latitude,
        longitude,
        productId: selectedProduct.productId,
        maxDistance,
        showUnrelated,
        showOutOfRange
      }
    };
    return baseReportContext;
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(dataSink){
    return {
      users: dataSink.rows.map(r => this.mapUser(r))
    };
  }

  mapUser(user){
    return user;
  }

  createSubscriptionStrategy(options, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext(options), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || hasAnyOptionChanged(previousOptions, newOptions, ['position', 'selectedProduct', 'reportId', 'maxDistance', 'showUnrelated', 'showOutOfRange']);
  }

  transformOptions(options){
    if (typeof options.reportId === 'undefined'){
      throw new Error('reportId should be defined');
    }
    const {searchText } = options;
    const filterExpression = searchText ? `firstName like "*${searchText}*"` : 'true';
    return {...options, filterExpression};
  }
}
