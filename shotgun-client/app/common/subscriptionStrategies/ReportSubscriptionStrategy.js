import {debounce} from 'lodash';
export default class ReportSubsciptionStrategy{
  constructor(client, reportContext){
    this.client = client;
    this.reportContext = reportContext;
    this.subscribe = this.subscribe.bind(this);
    this.update = debounce(this.update.bind(this), 500);
  }

  subscribe(dataSink, options){
    this.subscribeCommand = this.client.subscribeToReport(this.reportContext, options, dataSink);
  }

  update(dataSink, options){
    if (this.subscribeCommand === undefined){
      throw new Error('No subscribe command found must call subscribe before we call update subscription');
    }
    this.client.updateSubscription(this.subscribeCommand.id, options, dataSink);
  }
}
