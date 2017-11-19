import Rx from 'rx-lite';
import RxDataSink from '../dataSinks/RxDataSink';

export const WaitForEventTypes = observable =>
    observable.filter(ev => RxDataSink.SNAPSHOT_COMPLETE === ev.Type)
        .timeout(10000, new Error('No snapshot complete event detected 10 seconds after update'));

export default WaitForEventTypes;
