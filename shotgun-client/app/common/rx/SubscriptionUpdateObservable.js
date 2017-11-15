import Rx from 'rx-lite';
import RxDataSink from '../dataSinks/RxDataSink';

export default  suscriptionUpdateObservable = observable =>
    observable.filter(ev => !!~[RxDataSink.SUCCESS, RxDataSink.ERROR, RxDataSink.SNAPSHOT_COMPLETE].indexOf(ev.Type)) // filter for success or error events these are the results of the subsciption command
        .map(ev.Type == RxDataSink.ERROR ? Rx.Observable.throw(new Error(ev.error)) : ev) // throw error and terminate stream if we get an error event back
        .timeout(1000, new Error('No success or error event detected 1 seconds after subscribing')) //
        .filter(ev => RxDataSink.SNAPSHOT_COMPLETE === ev.Type)
        .timeout(10000, new Error('No snapshot complete event detected 10 seconds after update'));

