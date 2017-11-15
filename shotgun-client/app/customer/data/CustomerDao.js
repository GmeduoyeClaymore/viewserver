import * as FieldMappings from '../../common/constants/TableNames';
import DispatchingDataSink from '../../common/dataSinks/DispatchingDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import * as constants from '../../redux/ActionConstants';
import Logger from '../../viewserver-client/Logger';
import {forEach} from 'lodash';
import uuidv4 from 'uuid/v4';

//TODO - think this needs a Snapshotcomplete datasink + dispatching
export default class CustomerDao extends DispatchingDataSink {
    static DEFAULT_OPTIONS = (customerId) =>  {
      return {
        offset: 0,
        limit: 1,
        filterMode: 2, //Filtering
        filterExpression: `customerId == "${customerId}"`
      };
    };
  
    constructor(viewserverClient, dispatch){
      super();
      this.dispatch = dispatch;
      this.subscriptionStrategy = new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.CUSTOMER_TABLE_NAME);
    }

    subscribe(customerId){
      this.subscriptionStrategy.subscribe(this, CustomerDao.DEFAULT_OPTIONS(customerId));
    }

    dispatchUpdate(){
      this.dispatch({type: constants.UPDATE_CUSTOMER, customer: this.customer});
    }

    async addOrUpdateCustomer(customer){
      let customerRowEvent;

      Logger.info(`Adding customer schema is ${JSON.stringify(this.schema)}`);
      //TODO - tidy this up using lodash or similar
      const customerObject = {}
      forEach(this.schema, value => {
        const field = value.name;
        customerObject[field] = customer[field];
      });

      if (customerObject.customerId == undefined) {
        customerObject.customerId = uuidv4();
      }

      if (this.customer == undefined){
        Logger.info(`Adding customer ${JSON.stringify(customerObject)}`);
        customerRowEvent = this.createAddCustomerEvent(customerObject);
      } else {
        Logger.info(`Updating customer ${JSON.stringify(customerObject)}`);
        customerRowEvent = this.createUpdateCustomerEvent(customerObject);
      }
      const modifiedRows = await this.subscriptionStrategy.editTable(this, [customerRowEvent]);
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      return customerObject;
    }

    createAddCustomerEvent(customer){
      return {
        type: 0, // ADD
        columnValues: {
          ...customer
        }
      };
    }

    createUpdateCustomerEvent(customer){
      return {
        type: 1, // UPDATE
        columnValues: {
          ...customer
        }
      };
    }

    get customer(){
      return !this.rows || !this.rows.length ? undefined : this.rows[0];
    }
}
  
