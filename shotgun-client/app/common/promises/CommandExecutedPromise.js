import Logger from 'common/Logger';

export default class CommandExecutedPromise{
  constructor(){
    this.promise = new Promise(this._handlePromiseExecution.bind(this));
    this.onSuccess = this.onSuccess.bind(this);
    this.onError = this.onError.bind(this);
  }

  then () {
	    return this.promise.then.apply(this.promise, arguments);
  }

  _handlePromiseExecution(resolve, reject){
    this.resolve = arg => { resolve(arg); this.dispose(); };
    this.reject = arg => { reject(arg); this.dispose(); };
  }

  onError(message){
    Logger.warning(`Promise event handler returned error "${message}"`);
    this.reject(message);
  }

  onSuccess(commandResultId, message){
    Logger.info(`Command successfully executed with the following message "${message}"`);
    this.resolve();
  }

  dispose(){
  }
}
