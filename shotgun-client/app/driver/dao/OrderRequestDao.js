import ReportSubscriptionStrategy from 'common/subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {hasAnyOptionChanged} from 'common/dao';

export const isImplicitylChecked = (categoryObj, selectedProductCategories ) => {
  return !!selectedProductCategories.find(c=> isDescendendantOf(categoryObj, c));
};

export const isDescendendantOf = (parent, child)=> {
  const {path: childPath = ''} = child;
  const {path: parentPath = ''} = parent;
  return childPath.includes(parentPath + '>') && childPath.length > parentPath.length;
};

export default class OrderRequestDaoContext{
  static DEFAULT_POSITION = {
    latitude: 0,
    longitude: 0
  }
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    maxDistance: 0,
    showOutOfRange: true,
    position: OrderRequestDaoContext.DEFAULT_POSITION
  };


  constructor(client, options = {}) {
    this.client = client;
    this.options = {...OrderRequestDaoContext.OPTIONS, ...options};
    this.subscribeOnCreate = false;
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return 'orderRequestDao';
  }


  getReportContext({contentType, contentTypeOptions = {}, position = OrderRequestDaoContext.DEFAULT_POSITION, maxDistance = 0, showUnrelated = true, showOutOfRange = true}){
    if (typeof contentType === 'undefined') {
      return {};
    }
    const {latitude: driverLatitude, longitude: driverLongitude} = position;
    const {selectedProductIds} = contentTypeOptions;
    const baseReportContext =  {
      reportId: 'orderRequest',
      dimensions: {
        dimension_contentTypeId: [contentType.contentTypeId]
      },
      parameters: {
        driverLatitude,
        driverLongitude,
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

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(dataSink){
    return {
      driver: {
        orders: dataSink.rows.map(r => this.mapOrderRequest(r)),
      }
    };
  }

  mapOrderRequest(orderRequest){
    return {
      orderId: orderRequest.orderId,
      status: orderRequest.status,
      totalPrice: orderRequest.totalPrice,
      orderItem: {
        productId: orderRequest.productId,
        notes: orderRequest.notes,
        imageUrl: orderRequest.imageUrl,
      },
      contentType: {
        contentTypeId: orderRequest.contentTypeContentTypeId,
        name: orderRequest.contentTypeName,
        origin: orderRequest.contentTypeOrigin,
        destination: orderRequest.contentTypeDestination,
        noPeople: orderRequest.contentTypeNoPeople,
        fromTime: orderRequest.contentTypeFromTime,
        tillTime: orderRequest.contentTypeTillTime,
        noItems: orderRequest.contentTypeNoItems,
        rootProductCategory: orderRequest.contentTypeRootProductCategory,
        pricingStrategy: orderRequest.contentTypePricingStrategy
      },
      delivery: {
        from: orderRequest.from,
        till: orderRequest.till,
        distance: orderRequest.distance,
        duration: orderRequest.duration,
        noRequiredForOffload: orderRequest.noRequiredForOffload,
        deliveryId: orderRequest.deliveryId,
        origin: {
          flatNumber: orderRequest.originFlatNumber,
          line1: orderRequest.originLine1,
          city: orderRequest.originCity,
          postCode: orderRequest.originPostCode,
          latitude: orderRequest.originLatitude,
          longitude: orderRequest.originLongitude
        },
        destination: {
          flatNumber: orderRequest.destinationFlatNumber,
          line1: orderRequest.destinationLine1,
          city: orderRequest.destinationCity,
          postCode: orderRequest.destinationPostCode,
          latitude: orderRequest.destinationLatitude,
          longitude: orderRequest.destinationLongitude
        }
      }
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext(options), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    //TODO - probably needs to be updated as driver position changed but not sure how often
    return hasAnyOptionChanged(previousOptions, newOptions, ['contentType', 'location', 'maxDistance', 'showUnrelated', 'showOutOfRange']);
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
    const expressionArray = selectedProductCategories.filter(cat => !isImplicitylChecked(cat, selectedProductCategories)).map(this.toFilterExpression);
    expressionArray.push(`contentTypeRootProductCategory == "${contentType.rootProductCategory}"`);
    return expressionArray.join(' || ');
  }

  toFilterExpression({path}){
    return `path like "${path}*"`;
  }
}
