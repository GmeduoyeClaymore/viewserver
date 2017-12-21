import ReportSubscriptionStrategy from '../../common/subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';

export default class OrderSummaryDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    isCompleted: false
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...OrderSummaryDaoContext.OPTIONS, ...options};
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return 'orderSummaryDao';
  }

  getReportContext({userId, isCompleted}){
    return {
      reportId: 'orderSummary',
      parameters: {
        userId,
        isCompleted
      }
    };
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      customer: {
        orders: dataSink.rows.map(r => this.mapOrderSummary(r)),
      }
    };
  }

  mapOrderSummary(orderSummary){
    return {
      orderId: orderSummary.orderId,
      status: orderSummary.status,
      orderItem: {
        productId: orderSummary.productId,
        notes: orderSummary.notes,
        imageUrl: orderSummary.imageUrl,
      },
      delivery: {
        eta: orderSummary.eta,
        noRequiredForOffload: orderSummary.noRequiredForOffload,
        vehicleTypeId: orderSummary.vehicleTypeId,
        deliveryId: orderSummary.deliveryId,
        origin: {
          flatNumber: orderSummary.originFlatNumber,
          line1: orderSummary.originLine1,
          city: orderSummary.originCity,
          postCode: orderSummary.originPostCode,
          latitude: orderSummary.originLatitude,
          longitude: orderSummary.originLongitude
        },
        destination: {
          flatNumber: orderSummary.destinationFlatNumber,
          line1: orderSummary.destinationLine1,
          city: orderSummary.destinationCity,
          postCode: orderSummary.destinationPostCode,
          latitude: orderSummary.destinationLatitude,
          longitude: orderSummary.destinationLongitude
        }
      },
      payment: {
        paymentId: orderSummary.paymentId
      }
    };
  }

  createSubscriptionStrategy({userId, isCompleted}, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext({userId, isCompleted}), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.userId != newOptions.userId || previousOptions.isCompleted != newOptions.isCompleted;
  }

  transformOptions(options){
    if (typeof options.userId === 'undefined'){
      throw new Error('userId should be defined');
    }
    if (typeof options.isCompleted === 'undefined'){
      throw new Error('isCompleted  should be defined');
    }
    return options;
  }
}
