import Logger from '../viewserver-client/Logger';


export default class ClientTableEventPromise{
  constructor(coolRxDataSink, rowEvents){
    this.promise = new Promise(this._handlePromiseExecution.bind(this));
    this.onSuccess = this.onSuccess.bind(this);
    this.onError = this.onError.bind(this);
    this.onRowAdded = this.onRowAdded.bind(this);
    this.onRowUpdated = this.onRowUpdated.bind(this);
    this.initialRowEvents = rowEvents;
    this.pendingRowEvents = rowEvents;
    this.rowAddedSubscription = coolRxDataSink.onRowAddedObservable.subscribe(this.onRowAdded);
    this.rowUpdatedSubscription = coolRxDataSink.onRowUpdatedObservable.subscribe(this.onRowUpdated);
    this.rowsForTableEventType = {};
  }

  then () {
	    return this.promise.then.apply(this.promise, arguments);
  }

  _handlePromiseExecution(resolve, reject){
    this.resolve = arg => { resolve(arg); this.dispose(); };
    this.reject = arg => { reject(arg); this.dispose(); };
  }

  onRowAdded(row){
    Logger.info(`Row added - ${row.rowId} -  + ${JSON.stringify(row)}`);
    this.handleRowOperation(0, row);
  }
  onRowUpdated(row){
    Logger.info(`Row Updated - ${row.rowId} -  + ${JSON.stringify(row)}`);
    this.handleRowOperation(1, row);
  }

  handleRowOperation(type, row){
    Logger.info('Handling row operation');
    const events  = this.pendingRowEvents.filter(ev => ev.type === type);
    if (!events || !events.length){
      Logger.info(`No row operations exepected of type ${type}`);
      return;
    }
    for (const event of events){
      for (const columnName of Object.keys(event.columnValues)){
        if (row[columnName] != event.columnValues[columnName]){
          Logger.info(`Value for column ${columnName} differs on row ${JSON.stringify(row)} and event ${JSON.stringify(event)}`);
          return;
        }
      }
      this.rowsForTableEventType[type] = this.rowsForTableEventType[type] ? [...this.rowsForTableEventType[type], row] : [row];
      this.pendingRowEvents = this.pendingRowEvents.filter(ev => ev !== event);
      break;
    }
    if (this.pendingRowEvents.length === 0){
      Logger.info(`Resolving table edit promise after successfully executing "${this.initialRowEvents.length}"`);
      this.resolve(this.rowsForTableEventType);
    }
  }

  onError(message){
    Logger.error(`Promise event handler returned error "${message}"`);
    this.reject(message);
  }

  onSuccess(commandResultId, message){
    Logger.info(`Table edit result successfully executed with the following message "${message}"`);
  }

  dispose(){
    this.rowAddedSubscription.dispose();
    this.rowUpdatedSubscription.dispose();
  }
}
