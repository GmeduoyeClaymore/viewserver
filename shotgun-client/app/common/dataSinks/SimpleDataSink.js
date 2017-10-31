import DataSink from './DataSink';
import CoolRxDataSink from './CoolRxDataSink';

export default class SimpleDataSink extends DataSink(CoolRxDataSink){
  constructor(){
    super();
  }
}
