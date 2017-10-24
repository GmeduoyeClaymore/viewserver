import Logger from '../viewserver-client/Logger';
import {entries} from './utils';

const EMPTY_HOLDER = {};

export default class ClientTableEventPromise{
  constructor(coolRxDataSink){
    this.promise = new Promise(this._handlePromiseExecution.bind(this));
    this.onSuccess = this.onSuccess.bind(this);
    this.onError = this.onError.bind(this);
    this.onRowAdded = this.onRowAdded.bind(this);
    this.onRowUpdated = this.onRowUpdated.bind(this);
    this.pendingRowAdditions = {};
    this.pendingRowUpdates = {};
    this.operationListeners = {};
    this.operationListeners.U = this.pendingRowUpdates;
    this.operationListeners.A = this.pendingRowAdditions;
    this.rowAddedSubscription = coolRxDataSink.onRowAddedObservable.subscribe(this.onRowAdded);
    this.rowUpdatedSubscription = coolRxDataSink.onRowUpdatedObservable.subscribe(this.onRowUpdated);
    this.parseMessagePart = this.parseMessagePart.bind(this);
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
    this.handleRowOperation('A', row);
  }
  onRowUpdated(row){
    Logger.info(`Row Updated - ${row.rowId} -  + ${JSON.stringify(row)}`);
    this.handleRowOperation('U', row);
  }

  handleRowOperation(type, row){
    Logger.info('Handling row operation');
    const listenerForType  = this.operationListeners[type];
    if (!listenerForType){
      throw new Error(`Unable to find listener for event type ${type}`);
    }
    if (row.rowId in listenerForType){
      Logger.info(`found listened for row ${row.rowId} row ${JSON.stringify(row)}`);
      listenerForType[row.rowId] = row;
    } else {
      Logger.info(`Observer not listened for row ${JSON.stringify(row)}`);
    }
    for (const [op, listener] of entries(this.operationListeners)) {
      for (const [rw, actions] of entries(listener)) {
        if (actions === EMPTY_HOLDER){
          Logger.info(`found empty action for operation ${op} row ${rw}`);
          return;
        }
      }
    }
    this.resolve(listenerForType);
  }

  onError(message){
    Logger.error(`Promise event handler returned error "${message}"`);
    this.reject(message);
  }

  onSuccess(commandResultId, message){
    if (!message){
      Logger.error('Expecting message from command execution to indicate what has changed in the table so we can listen for changes on the client side');
    } else {
      Logger.info(`Table edit result successfully executed with the following message "${message}"`);
    }
    const messageParts = message.split(',');
    messageParts.map(this.parseMessagePart);
  }

  parseMessagePart(token){
    if (!token){
      return;
    }
    const operation = token.charAt(0);
    const operationListeners = this.operationListeners[operation];
    if (!operationListeners){
      Logger.error(`Unable to find listener for operation ${operation}`);
    } else {
      operationListeners[token.substring(1)] = EMPTY_HOLDER;
    }
  }

  dispose(){
    this.rowAddedSubscription.dispose();
    this.rowUpdatedSubscription.dispose();
  }
}
