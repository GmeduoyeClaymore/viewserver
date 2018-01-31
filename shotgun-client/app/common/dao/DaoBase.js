import Logger from 'common/Logger';
import Rx from 'rxjs/Rx';
import * as crx from 'common/rx';
import {page} from 'common/dao/DaoExtensions';
import {isEqual} from 'lodash';
import RxDataSink from '../../common/dataSinks/RxDataSink';

export default class Dao {
  constructor(daoContext) {
    this.daoContext = daoContext;
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.countSubject = new Rx.Subject();
    this.options = this.daoContext.defaultOptions;
    if (this.daoContext.extendDao){
      this.daoContext.extendDao(this);
    }
    this.name = daoContext.name;
    this.page = page(this);
    this.handleConnectionStatus = this.handleConnectionStatus.bind(this);
    this.updateSubscription = this.updateSubscription.bind(this);
    this.resetData = this.resetData.bind(this);
    this.updateOptions = this.updateOptions.bind(this);
    this.crx = crx;//force this to load
    if (!this.daoContext.client){
      Logger.warning('Unable to link up reconnect event as cannot find client in DAO context');
      this.daoContext.client.connection.connectionObservable.subscribe(this.handleConnectionStatus);
    }
  }

  handleConnectionStatus(status){
    if (status){
      Logger.info('Handling reconnection logic');
      this.updateSubscription(this.options, true);
    }
  }
    
  get observable(){
    return this.subject;
  }
    
  get optionsObservable(){
    return this.optionsSubject;
  }
    
  get countObservable(){
    return this.countSubject;
  }

  async updateOptions(options){
    this.options = {...this.options, ...options};
  }

  resetData(){
    this.dataSink.onDataReset();
  }

  async updateSubscription(options, force){
    const newOptions = {...this.options, ...options};
    if (this.daoContext.doesSubscriptionNeedToBeRecreated(this.options, newOptions) || !this.subscriptionStrategy || force){
      if (this.subscriptionStrategy){
        this.subscriptionStrategy.dispose();
      }
      if (this.rowEventSubscription){
        this.rowEventSubscription.unsubscribe();
      }
      if (this.countSubscription){
        this.countSubscription.unsubscribe();
      }
      this.dataSink = this.daoContext.createDataSink(newOptions);
      this.subscriptionStrategy = this.daoContext.createSubscriptionStrategy(newOptions, this.dataSink);

      this.rowEventObservable = this.dataSink.dataSinkUpdated.filterRowEvents();
      this.countSubscription = this.dataSink.dataSinkUpdated.filter(ev => ev.Type === RxDataSink.TOTAL_ROW_COUNT).subscribe(ev => this.countSubject.next(ev.count));
      const _this = this;
      this.domainEventObservable = this.rowEventObservable.map(ev => this.daoContext.mapDomainEvent(ev, _this.dataSink));
      if (this.daoContext.getDataFrequency){
        this.domainEventObservable = this.domainEventObservable.debounceTime(this.daoContext.getDataFrequency());
      }
      this.rowEventSubscription = this.domainEventObservable.subscribe(ev => this.subject.next(ev));
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
      const snapshotPromise = this.dataSink.dataSinkUpdated.waitForSnapshotComplete().toPromise();
      this.subscriptionStrategy.updateSubscription(optionsMessage);

      Logger.info('!!!!!Waiting for snapshot complete!!!!');
      const result = await snapshotPromise;
      Logger.info('!!!!!Completed snapshot complete!!!!');
      return result;
    } catch (error){
      return Promise.reject(error);
    }
  }
}

