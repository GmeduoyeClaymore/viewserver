import Logger from '../viewserver-client/Logger';

export default class PromiseEventHandler {
  constructor(){
    this.promise = new Promise(this._handlePromiseExecution.bind(this));
    this.onSuccess = this.onSuccess.bind(this);
    this.onError = this.onError.bind(this);
    this.onSnapshotComplete = this.onSnapshotComplete.bind(this);
  }

  then () {
	    return this.promise.then.apply(this.promise, arguments);
  }

  _handlePromiseExecution(resolve, reject){
    this.resolve = resolve;
    this.reject = reject;
  }

  onSnapshotComplete(){
    this.resolve();
  }

  onError(message){
    Logger.error(`Promise event handler returned error "${message}"`);
    this.reject(message);
  }

  onSuccess(){
    this.resolve();
  }
}
