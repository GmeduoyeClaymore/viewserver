import RxDataSink from '../dataSinks/RxDataSink';

const DOMAIN_EVENT_TYPES = [RxDataSink.ROW_ADDED, RxDataSink.ROW_UPDATED, RxDataSink.ROW_REMOVED];

export const  RowEventFilteredObservable = observable =>
    observable.filter(ev => !!~DOMAIN_EVENT_TYPES.indexOf(ev.Type));

export default RowEventFilteredObservable;

