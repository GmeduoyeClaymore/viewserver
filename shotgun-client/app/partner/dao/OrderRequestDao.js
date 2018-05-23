import ReportSubscriptionStrategy from 'common/subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {hasAnyOptionChanged} from 'common/dao';
import {OrderStatuses} from 'common/constants/OrderStatuses';

export default class OrderRequestDao{
  static DEFAULT_POSITION = {
    latitude: 0,
    longitude: 0
  };
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    maxDistance: 0,
    showOutOfRange: true,
    position: OrderRequestDao.DEFAULT_POSITION
  };

  static PARTNER_AVAILABLE_ORDERS_DEFAULT_OPTIONS = {
    columnsToSort: [{ name: 'requiredDate', direction: 'asc' }, { name: 'lastModified', direction: 'asc' }]
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...OrderRequestDao.OPTIONS, ...options};
    this.subscribeOnCreate = false;
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return 'orderRequestDao';
  }

  isImplicitylChecked(categoryObj, selectedProductCategories) {
    return !!selectedProductCategories.find(c=> this.isDescendendantOf(categoryObj, c));
  }

  isDescendendantOf(parent, child){
    const {path: childPath = ''} = child;
    const {path: parentPath = ''} = parent;
    return childPath.includes(parentPath + '>') && childPath.length > parentPath.length;
  }

  getReportContext({contentType, contentTypeOptions = {}, position = OrderRequestDao.DEFAULT_POSITION, maxDistance = 0, showUnrelated = true, showOutOfRange = true}){
    if (typeof contentType === 'undefined') {
      return {};
    }
    const {latitude: partnerLatitude, longitude: partnerLongitude} = position;
    const {selectedProductIds} = contentTypeOptions;
    const baseReportContext =  {
      reportId: 'orderRequest',
      dimensions: {
        dimension_contentTypeId: [contentType.contentTypeId],
        dimension_status: [OrderStatuses.PLACED]
      },
      excludedFilters: {
        dimension_customerUserId: '@userId'
      },
      parameters: {
        partnerLatitude,
        partnerLongitude,
        maxDistance,
        showUnrelated,
        showOutOfRange
      }
    };
  
    if (selectedProductIds){
      baseReportContext.dimensions.dimension_productId = selectedProductIds;
    }

    return baseReportContext;
  }

  createDataSink = () => {
    return new RxDataSink(this._name);
  }

  mapDomainEvent(dataSink){
    return {
      orders: dataSink.orderedRows.map(r => this.mapOrderRequest(r)),
    };
  }

  mapOrderRequest(orderRequest){
    const {orderDetails, partner_firstName, partner_lastName, partner_ratingAvg, partner_imageUrl, customer_firstName, customer_lastName, customer_ratingAvg} = orderRequest;

    const assignedPartner = orderDetails.assignedPartner ? {
      ...orderDetails.assignedPartner,
      firstName: partner_firstName,
      lastName: partner_lastName,
      ratingAvg: partner_ratingAvg,
      imageUrl: partner_imageUrl
    } : undefined;

    return {
      ...orderDetails,
      assignedPartner,
      customer: {
        firstName: customer_firstName,
        lastName: customer_lastName,
        ratingAvg: customer_ratingAvg
      }
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext(options), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return hasAnyOptionChanged(previousOptions, newOptions, ['contentType', 'location', 'maxDistance', 'showUnrelated', 'showOutOfRange', 'contentTypeOptions']);
  }

  transformOptions(options){
    if (typeof options.contentType === 'undefined'){
      throw new Error('contentTypeId should be defined');
    }
    options.filterExpression = this.generateFilterExpression(options);
    return options;
  }

  generateFilterExpression(opts){
    const {contentTypeOptions = {}, contentType = {}} = opts;
    const {selectedProductCategories = []} = contentTypeOptions;
    if (!selectedProductCategories.length){
      return undefined;
    }
    const expressionArray = [...selectedProductCategories.filter(cat => !this.isImplicitylChecked(cat, selectedProductCategories)).map(this.toFilterExpression)];
    expressionArray.push(`contentTypeRootProductCategory == "${contentType.rootProductCategory}"`);
    return expressionArray.join(' || ');
  }

  toFilterExpression({path}){
    return `path like "${path}*"`;
  }
}
