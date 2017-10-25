import Logger from '../viewserver-client/Logger';

export default class ExternallyResolvedPromise{
  constructor(){
    this.promise = new Promise(this._handlePromiseExecution.bind(this));
  }

  _handlePromiseExecution(resolve, reject){
    this.resolve = resolve;
    this.reject = reject;
  }

  then () {
	    return this.promise.then.apply(this.promise, arguments);
  }

  reject(message){
    Logger.error(`Promise event handler returned error "${mmessage}"`);
    this.reject(message);
  }

  resolve(commandResultId){
    Logger.error(`Successfully executed command"${commandResultId}"`);
    this.resolve();
  }
}
