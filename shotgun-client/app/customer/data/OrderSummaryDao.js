import DataSink from '../../common/dataSinks/DataSink';
import CoolRxDataSink from '../../common/dataSinks/CoolRxDataSink';
import ReportSubscriptionStrategy from '../../common/subscriptionStrategies/ReportSubscriptionStrategy';
import Rx from 'rx-lite';

export default class OrderSummaryDao extends DataSink(CoolRxDataSink){
  constructor(viewserverClient, customerId){
    super();
    this.viewserverClient = viewserverClient;
    this.customerId = customerId;

    const reportContext = {
      reportId: 'orderSummary',
      parameters: {
        customerId/*,
        isCompleted: false*/
      }
    };
    this.subscriptionStrategy = new ReportSubscriptionStrategy(viewserverClient, reportContext);
    this.subscriptionStrategy.subscribe(this, {limit: 1});
  }
}
