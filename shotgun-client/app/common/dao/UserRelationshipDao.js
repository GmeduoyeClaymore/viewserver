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
    showOutOfRange: true,
    position: UserRelationshipDaoContext.DEFAULT_POSITION,
    maxDistance: 0
  };

  constructor(client, name = 'userRelationshipDao', options = {}) {
    this.client = client;
    this.options = {...UserRelationshipDaoContext.OPTIONS, ...options};
    this.subscribeOnCreate = false;
    this._name = name;
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return this._name;
  }

  getReportContext({reportId, selectedProduct, position = UserRelationshipDaoContext.DEFAULT_POSITION, maxDistance = 0, showUnrelated = false, showOutOfRange = true}){
    const {latitude = 0, longitude = 0} = position;
    const baseReportContext =  {
      reportId,
      dimensions: selectedProduct ? {
        dimension_productId: [selectedProduct.productId]
      } : undefined,
      parameters: {
        latitude,
        longitude,
        showUnrelated,
        maxDistance,
        showOutOfRange
      }
    };
    return baseReportContext;
  }

  createDataSink = () => {
    return new RxDataSink(this._name);
  }

  mapDomainEvent(dataSink){
    return {
      users: dataSink.rows
    };
  }


  mapUser(user){
    if (!user){
      return user;
    }
    return {...user, status: user.userStatus};
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
      const virtualContactNo = await this.client.invokeJSONCommand('phoneCallController', 'getVirtualNumber', {userId});
      PhoneCallService.call(virtualContactNo);
    };
  }
}
