import {debounce} from 'lodash';
export default class ReportSubsciptionStrategy{
  constructor(client, reportContext, dataSink){
    this.client = client;
    this.dataSink = dataSink;
    this.reportContext = reportContext;
    this.subscribe = this.subscribe.bind(this);
    this.dispose = this.dispose.bind(this);
    this.subscribeOrUpdate = debounce(this.subscribeOrUpdate.bind(this), 500);
  }

  subscribeOrUpdate(options){
    if (this.subscribeCommand === undefined){
      this.subscribeCommand = this.client.subscribeToReport(this.reportContext, options, this.dataSink);
    } else {
      this.client.updateSubscription(this.subscribeCommand.id, options, dataSink);
    }
  }

  dispose(){
    if (this.subscribeCommand && this.dataSink){
      this.client.unsubscribe(this.subscribeCommand.id, this.dataSink);
    }
  }
}
