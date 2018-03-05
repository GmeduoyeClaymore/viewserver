import CommandExecutedPromise from '../../common/promises/CommandExecutedPromise';
import {debounce} from 'lodash';
export default class OperatorSubscriptionStrategyStrategy{
  constructor(client, path, dataSink){
    this.client = client;
    this.dataSink = dataSink;
    this.path = path;
    this.dispose = this.dispose.bind(this);
    this.updateSubscription = this.updateSubscription.bind(this);
  }

  async updateSubscription(options){
    if (this.subscribeCommand === undefined){
      this.subscribeCommand = this.client.subscribe(this.path, options, this.dataSink);
    } else {
      this.client.updateSubscription(this.subscribeCommand.id, options, this.dataSink);
    }
    return this.dataSink.dataSinkUpdated.waitForSuccess().toPromise();
  }

  dispose(){
    if (this.subscribeCommand && this.dataSink){
      this.client.unsubscribe(this.subscribeCommand.id, this.dataSink);
    }
  }
}
