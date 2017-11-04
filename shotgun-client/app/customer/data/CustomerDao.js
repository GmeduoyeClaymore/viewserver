import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/dataSinks/DataSink';
import SnapshotCompletePromise from '../../common/promises/SnapshotCompletePromise';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import uuidv4 from 'uuid/v4';

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
      this.subscriptionStrategy = new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.CUSTOMER_TABLE_NAME);
      this.viewserverClient = viewserverClient;
      this.subscriptionStrategy.subscribe(this, CustomerDao.DEFAULT_OPTIONS(customerId));
    }

    static generateCustomerId(){
      return uuidv4();
    }

    async addOrUpdateCustomer(args){
      let customerRowEvent;
      if (this.customer !== undefined){
        Logger.info(`Adding customer ${JSON.stringify(args)}`);
        customerRowEvent = this.createAddCustomerEvent(args);
      } else {
        Logger.info(`Updating customer ${JSON.stringify(args)}`);
        customerRowEvent = this.createUpdateCustomerEvent(args);
      }
      const modifiedRows = await this.subscriptionStrategy.editTable(this, [customerRowEvent]);
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      return modifiedRows;
    }

    createAddCustomerEvent(args){
      return {
        type: 0, // ADD
        columnValues: args
      };
    }

    createUpdateCustomerEvent(args){
      return {
        type: 1, // UPDATE
        columnValues: args
      };
    }

    get customer(){
      return  this.rows.first();
    }
}
  
