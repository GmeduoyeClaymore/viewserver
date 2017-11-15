import Logger from '../../common/Logger';
import Rx from 'rx-lite';
import RowFilteredObservable from '../../common/rx/RowFilteredObservable';

export default class Dao {
    constructor(daoContext) {
      this.dispatch = dispatch;
      this.daoContext = daoContext;
      this.parentCategoryId = undefined;
      this.subject = new Rx.Subject();
      this.options = this.daoContext.defaultOptions;
      if (this.daoContext.extendDao){
        this.daoContext.extendDao(this);
      }
    }
    updatOrSubscribe(options){
        if (this.daoContext.doesSubscriptionNeedToBeRecreated(this.options, options)){
            if (this.subscriptionStrategy){
                this.subscriptionStrategy.dispose();
            }
            if (this.rowEventSubscription){
                this.rowEventSubscription.unsubscribe();
            }
            this.dataSink = this.daoContext.createDataSink(options);
            this.subscriptionStrategy = this.daoContext.createSubscriptionStrategy(options);
           
            this.rowEventObservable = RowFilteredObservable(this.dataSink.dataSinkUpdated());
            this.rowEventSubscription = this.rowEventObservable.map(ev => this.daoContext.mapDomainEvent(ev, dataSink)).subscribe(this.subject.onNext);
            Logger.info(`Updating subscription for  ${this.daoContext.name}`);
        }
        const newOptions = {...this.options, ...options};
        try {
            this.options = newOptions;
            Logger.info(`Updating options to ${JSON.stringify(this.options)}`);
            this.subscriptionStrategy.updatOrSubscribe(this.dataSink, this.daoContext.transformOptions(this.options));
        } catch (error){
            return Promise.reject(error);
        }
        return SuscriptionUpdateObservable(this.dataSink.dataSinkUpdated()).toPromise();
    }
}

