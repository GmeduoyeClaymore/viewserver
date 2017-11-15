
import RxDataSink from '../../common/dataSinks/RxDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';

const createAddPaymentCardEvent = (paymentCard) => {
  return {
    type: 0, // ADD
    columnValues: {
      ...paymentCard
    }
  };
};

const createUpdatePaymentCardEvent = (paymentCard) =>{
  return {
    type: 1, // UPDATE
    columnValues: {
      ...paymentCard
    }
  };
};

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

  extendDao(dao){
    dao.addOrUpdatePaymentCard = async(customerId, paymentCard) =>{
      const {dataSink, subscriptionStrategy} = dao;
      const {schema} = dataSink;
      let paymentCardRowEvent;
      const customer = dataSink.rows[0];
  
      Logger.info(`Adding paymentCard schema is ${JSON.stringify(schema)}`);
      const paymentCardObject = {};
      forEach(schema, value => {
        const field = value.name;
        paymentCardObject[field] = paymentCard[field];
      });
  
      paymentCardObject.customerId = customerId;
  
      if (paymentCardObject.paymentId == undefined) {
        paymentCardObject.paymentId = uuidv4();
      }
  
      if (customer == undefined){
        Logger.info(`Adding paymentCard ${JSON.stringify(paymentCardObject)}`);
        paymentCardRowEvent = createAddPaymentCardEvent(paymentCardObject);
      } else {
        Logger.info(`Updating paymentCard ${JSON.stringify(paymentCardObject)}`);
        paymentCardRowEvent = createUpdatePaymentCardEvent(paymentCardObject);
      }
      const modifiedRows = await subscriptionStrategy.editTable(dataSink, [paymentCardRowEvent]);
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      return await dao.rowEventObservable.filter(row => row.paymentId === paymentCardRowEvent.columnValues.paymentId).timeout(5000, 'Could not detect created order in 5 seconds').toPromise();
    };
  }
}

