import Logger from '../viewserver-client/Logger';

export default class PromiseEventHandler extends Promise {  

    constructor(){
        super((resolve,reject) => this._handlePromiseExecution.bind(this)(resolve,reject))
    }

    _handlePromiseExecution(resolve,reject){
        this.resolve = resolve;
        this.reject = reject;
    }

    onSnapshotComplete(){
        this.resolve;
    }

    onError(message){
        Logger.error(`Promise event handler returned error "${message}"`)
        this.reject(message);
    }

    onSuccess(commandResultId){
        Logger.error(`Successfully executed command"${commandResultId}"`)
        this.resolve();
    }
   
};