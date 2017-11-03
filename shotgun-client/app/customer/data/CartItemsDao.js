import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/dataSinks/DataSink';
import CoolRxDataSink from '../../common/dataSinks/CoolRxDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';

export default class CartItemsDao extends DataSink(CoolRxDataSink) {
  static DEFAULT_OPTIONS = (customerId) => {
    return {
      offset: 0,
      limit: 20,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      filterExpression: `customerId == "${customerId}" && orderId == null`,
      flags: undefined
    };
  };

  constructor(viewserverClient, customerId) {
    super();
    this.subscriptionStrategy = new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.ORDER_ITEM_TABLE_NAME);
    this.subscriptionStrategy.subscribe(this, CartItemsDao.DEFAULT_OPTIONS(customerId));
  }
}
