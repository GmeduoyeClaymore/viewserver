import Logger from 'common/Logger';
import Rx from 'rx-lite';
import RowEventFilteredObservable from 'common/rx/RowEventFilteredObservable';
import SubscriptionUpdateObservable from 'common/rx/SubscriptionUpdateObservable';
import {page} from 'common/dao/DaoExtensions';
export default class Dao {
    constructor(daoContext) {
      this.daoContext = daoContext;
      this.parentCategoryId = undefined;
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
        if (this.daoContext.doesSubscriptionNeedToBeRecreated(this.options, options) || !this.subscriptionStrategy){
            if (this.subscriptionStrategy){
                this.subscriptionStrategy.dispose();
            }
            if (this.rowEventSubscription){
                this.rowEventSubscription.unsubscribe();
            }
            this.dataSink = this.daoContext.createDataSink(options);
            this.subscriptionStrategy = this.daoContext.createSubscriptionStrategy(options);
           
            this.rowEventObservable = RowEventFilteredObservable(this.dataSink.dataSinkUpdated);
            this.rowEventSubscription = this.rowEventObservable.map(ev => this.daoContext.mapDomainEvent(ev, dataSink)).subscribe(this.subject.onNext);
            Logger.info(`Updating subscription for  ${this.daoContext.name}`);
        }
        const newOptions = {...this.options, ...options};
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

