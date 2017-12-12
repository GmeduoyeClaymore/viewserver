import CommandExecutedPromise from '../../common/promises/CommandExecutedPromise';
import {debounce, pickBy} from 'lodash';

export default class DataSourceSubscriptionStrategyStrategy{
  constructor(client, path, dataSink){
    this.client = client;
    this.dataSink = dataSink;
    this.path = path;
    this.editTable = this.editTable.bind(this);
    this.dispose = this.dispose.bind(this);
    this.updateSubscription = debounce(this.updateSubscription.bind(this), 500);
  }

  editTable(rowEvents){
    const commandExecutedPromise = new CommandExecutedPromise();
    const cleanedRowEvents = this.cleanColumnValues(rowEvents);
    this.client.editTable(`/datasources/${this.path}/${this.path}`, this.dataSink, cleanedRowEvents, commandExecutedPromise);
    return commandExecutedPromise;
  }

  updateSubscription(options){
    if (this.subscribeCommand === undefined){
      this.subscribeCommand = this.client.subscribeToDataSource(this.path, options, this.dataSink);
    } else {
      this.client.updateSubscription(this.subscribeCommand.id, options, this.dataSink);
    }
  }

  dispose(){
    if (this.subscribeCommand && this.dataSink){
      this.client.unsubscribe(this.subscribeCommand.id, this.dataSink);
    }
  }

  //removes all undefined values from the columnValues
  cleanColumnValues(rowEvents){
    rowEvents.forEach(r => {
      r.columnValues = pickBy(r.columnValues, v => v !== undefined);
    });

    return rowEvents;
  }
}
