
import RxDataSink from '../../common/dataSinks/RxDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';

export default class PaymentCardsDaoContext{
  constructor(client, options) {
    this.client = client;
    this.options = options;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 100,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      flags: undefined
    };
  }

  get name(){
      return 'paymentCards';
  }

  createDataSink(){
      return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      customer: {
        paymentCards: dataSink.rows
      }
    };
  }

  createSubscriptionStrategy(){
    return new DataSourceSubscriptionStrategy(client, FieldMappings.PAYMENT_CARDS_TABLE_NAME);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.customerId != newOptions.customerId;
  }

  transformOptions(options){
    if (typeof options.customerId === 'undefined'){
      throw new Error('customerId should be defined');
    }
    return {...options, filterExpression: `customerId == "${options.customerId}"`};
  }
}

