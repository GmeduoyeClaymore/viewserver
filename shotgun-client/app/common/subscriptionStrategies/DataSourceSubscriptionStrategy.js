import CommandExecutedPromise from '../../common/promises/CommandExecutedPromise';

export default class DataSourceSubscriptionStrategyStrategy{
  constructor(client, path, dataSink){
    this.client = client;
    this.dataSink = dataSink;
    this.path = path;
    this.subscribeOrUpdate = this.subscribeOrUpdate.bind(this);
    this.editTable = this.editTable.bind(this);
    this.dispose = this.dispose.bind(this);
    this.subscribeOrUpdate = debounce(this.subscribeOrUpdate.bind(this), 500);
  }

  editTable(rowEvents){
    const commandExecutedPromise = new CommandExecutedPromise();
    this.client.editTable(`/datasources/${this.path}/${this.path}`, this.dataSink, rowEvents, commandExecutedPromise);
    return commandExecutedPromise;
  }

  subscribeOrUpdate(options){
    if (this.subscribeCommand === undefined){
      this.subscribeCommand = this.client.subscribeToDataSource(this.path, options, this.dataSink);
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
