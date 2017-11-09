import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/dataSinks/DataSink';
import CoolRxDataSink from '../../common/dataSinks/CoolRxDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';

export default class PaymentCardsDao extends DataSink(CoolRxDataSink) {
  static DEFAULT_OPTIONS = (customerId) => {
    return {
      offset: 0,
      limit: 100,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      filterExpression: `customerId == "${customerId}"`,
      flags: undefined
    };
  };

  constructor(viewserverClient, customerId) {
    super();
    this.subscriptionStrategy = new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.PAYMENT_CARDS_TABLE_NAME);
    this.subscriptionStrategy.subscribe(this, PaymentCardsDao.DEFAULT_OPTIONS(customerId));
  }

  //TODO - functionality to add and remove cards
}
