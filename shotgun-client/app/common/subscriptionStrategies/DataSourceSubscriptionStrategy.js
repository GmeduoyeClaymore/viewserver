import TableEditPromise from '../../common/promises/TableEditPromise';

export default class DataSourceSubscriptionStrategyStrategy{
  constructor(client, path){
    this.client = client;
    this.path = path;
    this.subscribe = this.subscribe.bind(this);
    this.editTable = this.editTable.bind(this);
  }

  subscribe(dataSink, options){
    this.subscribeCommand = this.client.subscribeToDataSource(this.path, options, dataSink);
  }

  editTable(dataSink, rowEvents){
    const tableEditPromise = new TableEditPromise();
    this.client.editTable(`/datasources/${this.path}/${this.path}`, dataSink, rowEvents, tableEditPromise);
    return tableEditPromise;
  }

  update(dataSink, options){
    if (!this.subscribeCommand){
      throw new Error('No subscribe command found must call subscribe before we call update subscription');
    }
    this.updateSubscribeCommand = this.client.updateSubscription(this.subscribeCommand.id, options, dataSink);
  }
}
