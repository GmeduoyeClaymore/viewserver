import Logger from '../viewserver-client/Logger';

export default class ExternallyResolvedPromise extends Promise {  

    constructor(){
        super((resolve,reject) => this._handlePromiseExecution.bind(this)(resolve,reject))
    }

    _handlePromiseExecution(resolve,reject){
        this.resolve = resolve;
        this.reject = reject;
    }

    reject(message){
        Logger.error(`Promise event handler returned error "${mmessage}"`)
        this.reject(message);
    }

    resolve(commandResultId){
        Logger.error(`Successfully executed command"${commandResultId}"`)
        this.resolve();
    }
   
};