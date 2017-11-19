import Logger from 'common/Logger';
import Rx from 'rx-lite';
import RowEventFilteredObservable from 'common/rx/RowEventFilteredObservable';
import SubscriptionUpdateObservable from 'common/rx/SubscriptionUpdateObservable';
import {page} from 'common/dao/DaoExtensions';
import {SubscribeWithSensibleErrorHandling} from 'common/rx';
export default class Dao {
    constructor(daoContext) {
      this.daoContext = daoContext;
      this.subject = new Rx.Subject();
      this.options = this.daoContext.defaultOptions;
      if (this.daoContext.extendDao){
        this.daoContext.extendDao(this);
      }
      this.name = daoContext.name;
      this.page = page(this);
      this.updateSubscription = this.updateSubscription.bind(this);
    }
    
    get observable(){
        return this.subject;
    }

    updateSubscription(options){
        const newOptions = {...this.options, ...options};
        if (this.daoContext.doesSubscriptionNeedToBeRecreated(this.options, newOptions) || !this.subscriptionStrategy){
            if (this.subscriptionStrategy){
                this.subscriptionStrategy.dispose();
            }
            if (this.rowEventSubscription){
                this.rowEventSubscription.dispose();
            }
            this.dataSink = this.daoContext.createDataSink(newOptions);
            this.subscriptionStrategy = this.daoContext.createSubscriptionStrategy(newOptions, this.dataSink);
           
            this.rowEventObservable = RowEventFilteredObservable(this.dataSink.dataSinkUpdated);
            const _this = this;
            this.rowEventSubscription = SubscribeWithSensibleErrorHandling(this.rowEventObservable.map(ev => _this.daoContext.mapDomainEvent(ev, _this.dataSink)), ev => _this.subject.onNext(ev));
            Logger.info(`Updating subscription for  ${this.daoContext.name}`);
        }
        
        try {
            this.options = newOptions;
            Logger.info(`Updating options to ${JSON.stringify(this.options)}`);
            this.subscriptionStrategy.updateSubscription(this.daoContext.transformOptions(this.options));
        } catch (error){
            return Promise.reject(error);
        }
        return SubscriptionUpdateObservable(this.dataSink.dataSinkUpdated).toPromise();
    }
}

