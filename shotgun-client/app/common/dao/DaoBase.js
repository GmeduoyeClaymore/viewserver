import Logger from 'common/Logger';
import Rx from 'rxjs/Rx';
import * as crx from 'common/rx';
import {page} from 'common/dao/DaoExtensions';
import {isEqual} from 'lodash';
export default class Dao {
  constructor(daoContext) {
    this.daoContext = daoContext;
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.options = this.daoContext.defaultOptions;
    if (this.daoContext.extendDao){
      this.daoContext.extendDao(this);
    }
    this.name = daoContext.name;
    this.page = page(this);
    this.updateSubscription = this.updateSubscription.bind(this);
    this.crx = crx;//force this to load
  }
    
  get observable(){
    return this.subject;
  }
    
  get optionsObservable(){
    return this.optionsSubject;
  }

  async updateSubscription(options){
    const newOptions = {...this.options, ...options};
    if (this.daoContext.doesSubscriptionNeedToBeRecreated(this.options, newOptions) || !this.subscriptionStrategy){
      if (this.subscriptionStrategy){
        this.subscriptionStrategy.dispose();
      }
      if (this.rowEventSubscription){
        this.rowEventSubscription.unsubscribe();
      }
      this.dataSink = this.daoContext.createDataSink(newOptions);
      this.subscriptionStrategy = this.daoContext.createSubscriptionStrategy(newOptions, this.dataSink);

      this.rowEventObservable = this.dataSink.dataSinkUpdated.filterRowEvents();
      const _this = this;
      this.rowEventSubscription = this.rowEventObservable.map(ev => this.daoContext.mapDomainEvent(ev, _this.dataSink)).subscribe(ev => this.subject.next(ev));
      Logger.info(`Updating subscription for  ${this.daoContext.name}`);
    }
        
    try {
      if (isEqual(this.options, newOptions)){
        return Promise.resolve();
      }
      this.options = newOptions;
      Logger.info(`Updating options to ${JSON.stringify(this.options)}`);
      this.optionsSubject.next(this.options);
      const optionsMessage = this.daoContext.transformOptions(this.options);
      this.subscriptionStrategy.updateSubscription(optionsMessage);
    } catch (error){
      return Promise.reject(error);
    }
    Logger.info('!!!!!Waiting for snapshot complete!!!!');
    const result = await this.dataSink.dataSinkUpdated.waitForSnapshotComplete().toPromise();
    Logger.info('!!!!!Completed snapshot complete!!!!');
    return result;
  }
}

