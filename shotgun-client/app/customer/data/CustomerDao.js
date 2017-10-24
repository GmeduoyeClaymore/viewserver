import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/DataSink';
import SnapshotCompletePromise from '../../common/SnapshotCompletePromise';
import OperatorSubscriptionStrategy from '../../common/OperatorSubscriptionStrategy';

export default class CustomerDao extends DataSink(SnapshotCompletePromise) {
    static DEFAULT_OPTIONS = (customerId) =>  {
      return {
        offset: 0,
        limit: 20,
        columnName: undefined,
        columnsToSort: undefined,
        filterMode: 2, //Filtering
        filterExpression: `C_ID == "${customerId}"`,
        flags: undefined
      };
    };
  
    constructor(viewserverClient, customerId){
      super();
      this.subscriptionStrategy = new OperatorSubscriptionStrategy(viewserverClient, FieldMappings.CUSTOMER_TABLE_NAME);
      this.viewserverClient = viewserverClient;
      this.subscriptionStrategy.subscribe(this, CustomerDao.DEFAULT_OPTIONS(customerId));
    }
   

    get customer(){
      return  this.rows.first();
    }
}
  
