export default class DaoContext{
    get defaultOptions(){
        throw new Error('This should be implemented in base class');
    }

    get name(){
        throw new Error('This should be implemented in base class');
    }

    createDataSink(options){
        throw new Error('This should be implemented in base class');
    }

    mapDomainEvent(event, dataSink){
        throw new Error('This should be implemented in base class');
    }

    createSubscriptionStrategy(options, dataSink){
        throw new Error('This should be implemented in base class');
    }

    doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
        throw new Error('This should be implemented in base class');
    }
}
