import ReportSubscriptionStrategy from 'common/subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {hasAnyOptionChanged} from 'common/dao';
import PhoneCallService from 'common/services/PhoneCallService';

export default class UserRelationshipDaoContext{
  static DEFAULT_POSITION = {
    latitude: 0,
    longitude: 0
  }

  static OPTIONS = {
    offset: 0,
    limit: 100,
    filterMode: 2,
    showUnrelated: true,
    showOutOfRange: false,
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

  getReportContext({reportId, selectedProduct = {}, position = UserRelationshipDaoContext.DEFAULT_POSITION, maxDistance = 0, showUnrelated = false, showOutOfRange = false}){
    const {latitude = 0, longitude = 0} = position;
    const baseReportContext =  {
      reportId: reportId + (showUnrelated ? 'All' : ''),
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
      users: this.orderRows(dataSink.rows).map(r => this.mapUser(r))
    };
  }

  orderRows(rows){ /* work around until the sort operator is fixed */
    const result = [...rows];
    result.sort((r1, r2) => {
      if (r1.userId < r2.userId){
        return -1;
      }
      if (r1.userId > r2.userId){
        return 1;
      }
      return 0;
    });
    return result;
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
    if (options.reportId.startsWith('usersForProduct') && !options.selectedProduct){
      throw new Error('Selected product must me specifed for users for product report');
    }
    const {searchText } = options;
    const filterExpression = searchText ? `firstName like "*${searchText}*"` : 'true';
    return {...options, filterExpression};
  }

  extendDao(dao){
    dao.updateRelationship = async ({targetUserId, relationshipStatus, relationshipType}) => {
      await this.client.invokeJSONCommand('userController', 'updateRelationship', {targetUserId, relationshipStatus, relationshipType});
    };

    dao.callUser = async ({userId}) => {
      const virtualContactNo = await this.client.invokeJSONCommand('phoneCallController', 'getVirtualNumber', userId);
      PhoneCallService.call(virtualContactNo);
    };
  }
}
