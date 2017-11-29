import * as TableNames from 'common/constants/TableNames';
import RxDataSink from 'common/dataSinks/RxDataSink';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';
import {forEach} from 'lodash';
import uuidv4 from 'uuid/v4';

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
  constructor(client, options = {}) {
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
    return 'paymentCardsDao';
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

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, TableNames.PAYMENT_CARDS_TABLE_NAME, dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.userId != newOptions.userId;
  }

  transformOptions(options){
    if (typeof options.userId === 'undefined'){
      throw new Error('userId should be defined');
    }
    return {...options, filterExpression: `userId == \"${options.userId}\"`};
  }

  extendDao(dao){
    dao.addOrUpdatePaymentCard = async({userId, paymentCard}) =>{
      const {dataSink, subscriptionStrategy} = dao;
      const schema = await dataSink.waitForSchema();
      let paymentCardRowEvent;
      const existingCard = dataSink.rows[0];
  
      Logger.info(`Adding paymentCard schema is ${JSON.stringify(schema)}`);
      const paymentCardObject = {};
      forEach(schema, value => {
        const field = value.name;
        paymentCardObject[field] = paymentCard[field];
      });
  
      paymentCardObject.userId = userId;
  
      if (paymentCardObject.paymentId == undefined) {
        paymentCardObject.paymentId = uuidv4();
      }
  
      if (existingCard == undefined){
        Logger.info(`Adding paymentCard ${JSON.stringify(paymentCardObject)}`);
        paymentCardRowEvent = createAddPaymentCardEvent(paymentCardObject);
      } else {
        Logger.info(`Updating paymentCard ${JSON.stringify(paymentCardObject)}`);
        paymentCardRowEvent = createUpdatePaymentCardEvent(paymentCardObject);
      }
      const promise = dao.rowEventObservable.filter(ev => ev.row.paymentId == paymentCardObject.paymentId).take(1).timeoutWithError(5000, new Error(`Could not detect creation of payment card ${paymentCardObject.paymentId} in 5 seconds`)).toPromise();
      const modifiedRows = await subscriptionStrategy.editTable([paymentCardRowEvent]);
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      return await promise;
    };
  }
}

