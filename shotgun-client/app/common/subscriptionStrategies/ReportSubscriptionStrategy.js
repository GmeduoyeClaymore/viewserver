import {debounce} from 'lodash';
export default class ReportSubscriptionStrategy{
  constructor(client, reportContext, dataSink){
    this.client = client;
    this.dataSink = dataSink;
    this.reportContext = reportContext;
    this.dispose = this.dispose.bind(this);
    this.updateSubscription = debounce(this.updateSubscription.bind(this), 500);
  }

  updateSubscription(options){
    if (this.subscribeCommand === undefined){
      this.subscribeCommand = this.client.subscribeToReport(this.reportContext, options, this.dataSink);
    } else {
      this.client.updateSubscription(this.subscribeCommand.id, options, this.dataSink);
    }
  }

  dispose(){
    if (this.subscribeCommand && this.dataSink){
      this.client.unsubscribe(this.subscribeCommand.id, this.dataSink);
    }
  }
}
