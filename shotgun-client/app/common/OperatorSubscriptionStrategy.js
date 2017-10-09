import Logger from '../viewserver-client/Logger';
export default class OperatorSubsciptionStrategy{

    constructor(client,path){
        this.client = client;
        this.path = path;
        this.subscribe = this.subscribe.bind(this);
        this.update = this.update.bind(this);
    }

    subscribe(dataSink,options){
        this.subscribeCommand = this.client.subscribe(this.path,options,dataSink);
    }

    update(dataSink,options){
        if(!this.subscribeCommand){
            throw new Error("No subscribe command found must call subscribe before we call update subscription")
        }
        this.updateSubscribeCommand = this.client.updateSubscription(this.subscribeCommand.id,options,dataSink);
    }

}