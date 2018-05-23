import ReportSubscriptionStrategy from '../subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from '../dataSinks/RxDataSink';
import FCM from 'react-native-fcm';

export default class NotificationsDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 100,
    columnsToSort: [{name: 'sentTime', direction: 'desc'}]
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...NotificationsDaoContext.OPTIONS, ...options};
  }

  get defaultOptions(){
    return this.options;
  }

  getReportContext(){
    return {
      reportId: 'notificationsReport',
      parameters: {
      }
    };
  }

  get canSubscribeWithoutLogin(){
    return false;
  }

  get subscribeOnCreate(){
    return true;
  }

  get name(){
    return 'notificationsDao';
  }

  createDataSink = () => {
    return new RxDataSink(this._name);
  }

  transformOptions(options){
    return options;
  }

  mapDomainEvent(dataSink, ev){
    if (dataSink.isSnapshotComplete && ev.Type == RxDataSink.ROW_ADDED){
      const {row} = ev;
      const {message, sentRemotely} = row;
      if (!sentRemotely){
        FCM.presentLocalNotification(message);
      }
    }
    return dataSink.rows;
  }

  createSubscriptionStrategy(options, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext(), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions){
    return !previousOptions;
  }
}
