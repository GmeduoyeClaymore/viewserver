import ReportSubscriptionStrategy from 'common/subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';
import isEqual from 'lodash';

import * as ContentTypes from 'common/constants/ContentTypes';

export const isImplicitylChecked = (categoryObj, selectedProductCategories ) => {
  return !!selectedProductCategories.find(c=> isDescendendantOf(categoryObj, c));
};

export const isDescendendantOf = (parent, child)=> {
  return child.path.includes(parent.path + '>') && child.path.length > parent.path.length;
};

export default class OrderRequestDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    maxDistance: 50
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


  getReportContext({contentTypeId, contentTypeOptions = {}, position = {}, maxDistance}){
    if (typeof contentTypeId === 'undefined') {
      return {};
    }
    const {driverLatitude, driverLongitude} = position;
    const {selectedProductIds} = contentTypeOptions;
    const baseReportContext =  {
      reportId: 'orderRequest',
      dimensions: {
        dimension_contentTypeId: [contentTypeId]
      },
      parameters: {
        driverLatitude,
        driverLongitude,
        maxDistance
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
    return !previousOptions || previousOptions.contentTypeId != newOptions.contentTypeId || !isEqual(previousOptions.location, newOptions.location) || previousOptions.maxDistance != newOptions.maxDistance;
  }

  transformOptions(options){
    if (typeof options.contentTypeId === 'undefined'){
      throw new Error('contentTypeId should be defined');
    }
    if (typeof options.position === 'undefined'){
      throw new Error('position  should be defined');
    }
    if (typeof options.maxDistance === 'undefined'){
      throw new Error('maxDistance  should be defined');
    }
    options.filterExpression = this.generateFilterExpression(options);
    return options;
  }

  generateFilterExpression(opts){
    const {contentTypeOptions = {}} = opts;
    const {selectedProductCategories = []} = contentTypeOptions;
    if (!selectedProductCategories.length){
      return undefined;
    }
    return selectedProductCategories.filter(cat => !isImplicitylChecked(cat, selectedProductCategories)).map(this.toFilterExpression).join(' || ');
  }

  toFilterExpression(searchText){
    return `path like "${searchText}*"`;
  }
}
