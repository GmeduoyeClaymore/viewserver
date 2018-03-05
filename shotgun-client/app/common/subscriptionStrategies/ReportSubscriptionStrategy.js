import {debounce} from 'lodash';
export default class ReportSubscriptionStrategy{
  constructor(client, reportContext, dataSink){
    this.client = client;
    this.dataSink = dataSink;
    if (!this.dataSink){
      throw new Error('Data sink must be specified');
    }
    this.reportContext = reportContext;
    if (!this.reportContext){
      throw new Error('report context must be specified');
    }
    this.dispose = this.dispose.bind(this);
    this.updateSubscription = debounce(this.updateSubscription.bind(this), 50);
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
