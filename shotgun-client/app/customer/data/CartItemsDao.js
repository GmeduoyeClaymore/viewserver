import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/DataSink';
import CoolRxDataSink from '../../common/CoolRxDataSink';
import OperatorSubscriptionStrategy from '../../common/OperatorSubscriptionStrategy';

export default class CartItemsDao extends DataSink(CoolRxDataSink){
    static DEFAULT_OPTIONS = (customerId) =>  {
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
  
    constructor(viewserverClient, customerId){
      super();
      this.subscriptionStrategy = new OperatorSubscriptionStrategy(viewserverClient, FieldMappings.CART_ITEMS_VIEW_NAME);
      this.viewserverClient = viewserverClient;
      this.customerId = customerId;
      this.subscriptionStrategy.subscribe(this, CartItemsDao.DEFAULT_OPTIONS(this.customerId));
    }
}
