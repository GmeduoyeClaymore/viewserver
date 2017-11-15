import * as FieldMappings from '../../common/constants/TableNames';
import * as constants from '../../redux/ActionConstants';
import DispatchingDataSink from '../../common/dataSinks/DispatchingDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import uuidv4 from 'uuid/v4';
import Logger from '../../viewserver-client/Logger';
import {forEach} from 'lodash';

export default class PaymentCardsDao extends DispatchingDataSink {
  static DEFAULT_OPTIONS = (customerId) => {
    return {
      offset: 0,
      limit: 100,
      filterMode: 2, //Filtering
      filterExpression: `customerId == "${customerId}"`
    };
  };

  constructor(viewserverClient, customerId, dispatch) {
    super();
    this.dispatch = dispatch;
    this.subscriptionStrategy = new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.PAYMENT_CARDS_TABLE_NAME);
    this.subscriptionStrategy.subscribe(this, PaymentCardsDao.DEFAULT_OPTIONS(customerId));
  }

  dispatchUpdate(){
    this.dispatch({type: constants.UPDATE_CUSTOMER, customer: {paymentCards: this.rows}});
  }

  async addOrUpdatePaymentCard(customerId, paymentCard){
    let paymentCardRowEvent;

    Logger.info(`Adding paymentCard schema is ${JSON.stringify(this.schema)}`);
    const paymentCardObject = {}
    forEach(this.schema, value => {
      const field = value.name;
      paymentCardObject[field] = paymentCard[field];
    });

    paymentCardObject.customerId = customerId;

    if (paymentCardObject.paymentId == undefined) {
      paymentCardObject.paymentId = uuidv4();
    }

    if (this.customer == undefined){
      Logger.info(`Adding paymentCard ${JSON.stringify(paymentCardObject)}`);
      paymentCardRowEvent = this.createAddPaymentCardEvent(paymentCardObject);
    } else {
      Logger.info(`Updating paymentCard ${JSON.stringify(paymentCardObject)}`);
      paymentCardRowEvent = this.createUpdatePaymentCardEvent(paymentCardObject);
    }
    const modifiedRows = await this.subscriptionStrategy.editTable(this, [paymentCardRowEvent]);
    Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
    return modifiedRows;
  }

  createAddPaymentCardEvent(paymentCard){
    return {
      type: 0, // ADD
      columnValues: {
        ...paymentCard
      }
    };
  }

  createUpdatePaymentCardEvent(paymentCard){
    return {
      type: 1, // UPDATE
      columnValues: {
        ...paymentCard
      }
    };
  }

  //TODO - functionality  remove cards
}
