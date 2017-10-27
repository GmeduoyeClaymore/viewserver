import DataSink from '../../common/DataSink';
import CoolRxDataSink from '../../common/CoolRxDataSink';

export default class CartSummaryDao extends DataSink(CoolRxDataSink){
  static DEFAULT_OPTIONS = (customerId) =>  {
    return {
      offset: 0,
      limit: 20,
      columnName: undefined,
      columnsToSort: undefined,
      /* filterMode: 2, //Filtering
       filterExpression: `customerId == "${customerId}" && orderId == null`,*/
      flags: undefined
    };
  };

  constructor(viewserverClient, customerId){
    super();
    this.viewserverClient = viewserverClient;
    this.customerId = customerId;

    const reportContext = {
      reportId: 'cartSummary',
      parameters: {
        customerId
      }
    };
    this.viewserverClient.subscribeToReport(reportContext, CartSummaryDao.DEFAULT_OPTIONS, this);
  }
}
