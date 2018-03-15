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
    this.snapshotCompleteSubject = new Rx.Subject();
    this.options = this.daoContext.defaultOptions;
    if (this.daoContext.extendDao){
      this.daoContext.extendDao(this);
    }
    this.name = daoContext.name;
    this.page = page(this);
    this.subscribed = false;
    this.handleConnectionStatus = this.handleConnectionStatus.bind(this);
    this.updateSubscription = this.updateSubscription.bind(this);
    this.resetSubscription = this.resetSubscription.bind(this);
    this.resetData = this.resetData.bind(this);
    this.updateOptions = this.updateOptions.bind(this);

    this.crx = crx;//force this to load
    if (!this.daoContext.client){
      Logger.warning('Unable to link up reconnect event as cannot find client in DAO context');
    } else {
      this.daoContext.client.loggedInObservable.subscribe(this.handleConnectionStatus);
    }
  }

  handleConnectionStatus(status){
    if (status && this.subscribed){
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
  get snapshotCompleteObservable(){
    return this.snapshotCompleteSubject;
  }

  async updateOptions(options){
    this.options = {...this.options, ...options};
  }

  async resetData(){
    this.dataSink.onDataReset();
    this.subject.next();
  }

  async resetSubscription(options) {
    if (this.dataSink){
      this.dataSink.onDataReset();
    }
    this.options = this.daoContext.defaultOptions;
    return this.updateSubscription(options);
  }

  async updateSubscription(options, force){
    const newOptions = {...this.options, ...options};
    if (this.daoContext.doesSubscriptionNeedToBeRecreated(this.options, newOptions) || !this.subscriptionStrategy || force){
      if (this.subscriptionStrategy){
        Logger.info(`Disposing of subscription - ${this.daoContext.name}`);
        this.subscriptionStrategy.dispose();
      }
      if (this.rowEventSubscription){
        Logger.info(`Disposing of row event subscription - ${this.daoContext.name}`);
        this.rowEventSubscription.unsubscribe();
      }
      if (this.countSubscription){
        Logger.info(`Disposing of row count subscription - ${this.daoContext.name}`);
        this.countSubscription.unsubscribe();
      }
      if (this.snapshotCompleteSubscription){
        Logger.info(`Disposing of snapshotcomplete subscription - ${this.daoContext.name}`);
        this.snapshotCompleteSubscription.unsubscribe();
      }
      this.dataSink = this.daoContext.createDataSink(newOptions);
      this.dataSink.name = this.daoContext.name;
      this.subscriptionStrategy = this.daoContext.createSubscriptionStrategy(newOptions, this.dataSink);

      this.rowEventObservable = this.dataSink.dataSinkUpdated.filterRowEvents();
      this.countSubscription = this.dataSink.dataSinkUpdated.filter(ev => ev.Type === RxDataSink.TOTAL_ROW_COUNT).subscribe(ev => this.countSubject.next(ev.count));
      this.snapshotCompleteSubscription = this.dataSink.dataSinkUpdated.filter(ev => !!~[RxDataSink.DATA_RESET, RxDataSink.SNAPSHOT_COMPLETE].indexOf(ev.Type)).subscribe(() => this.snapshotCompleteSubject.next(this.dataSink.isSnapshotComplete));

      this.rowEventSubscription =  (this.daoContext.adapt ?  this.daoContext.adapt(this.rowEventObservable) : this.rowEventObservable).subscribe((ev) => {
        if (this.dataSink.isSnapshotComplete || ev.Type == RxDataSink.DATA_RESET) {
          this.subject.next(this.daoContext.mapDomainEvent(this.dataSink));
        }
      });

      Logger.info(`Updating subscription for  ${this.daoContext.name}`);
    }

    try {
      if (isEqual(this.options, newOptions) && this.subscribed && !force){
        return Promise.resolve();
      }
      if (this.snapshotSubscription){
        Logger.info(`!!!!!Found in flight subscription to snapshot complete cancelling !!!! ${this.daoContext.name}`);
        this.snapshotSubscription.unsubscribe();
        this.snapshotSubscription = undefined;
        if (this.promiseReject){
          this.promiseReject('Operation cancelled by another subscription operation');
          this.promiseReject = undefined;
        }
      }
      this.options = newOptions;
      Logger.info(`Updating options to ${JSON.stringify(this.options)} ${this.daoContext.name}`);
      this.optionsSubject.next(this.options);
      const optionsMessage = this.daoContext.transformOptions(this.options);
      const snapshotObservable = this.dataSink.dataSinkUpdated.waitForSnapshotComplete(10000);
      const _this = this;
      const result = new Promise((resolve, reject) => {
        _this.promiseReject = resolve;
        _this.snapshotSubscription = snapshotObservable.subscribe(
          ev => {
            Logger.info(`!!!!!Completed snapshot complete!!!! ${this.daoContext.name}`);
            resolve(ev);
            this.subscribed = true;
          },
          err => reject(err)
        );
      });
      this.subscriptionStrategy.updateSubscription(optionsMessage);
      Logger.info(`!!!!!Waiting for snapshot complete!!!! ${this.daoContext.name}`);
      return result;
    } catch (error){
      Logger.warning(error);
      return Promise.reject(error);
    }
  }
}

