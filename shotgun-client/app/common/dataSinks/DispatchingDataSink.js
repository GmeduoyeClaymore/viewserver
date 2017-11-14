import DataSink from '../../common/dataSinks/DataSink';
import Logger from '../../viewserver-client/Logger';

export default class DispatchingDataSink extends DataSink(null){
  onSnapshotComplete(){
    super.onSnapshotComplete();
    this.dispatchUpdate();
  }

  onDataReset(){
    super.onDataReset();
    this.dispatchUpdate();
  }

  onRowAdded(rowId, row){
    super.onRowAdded(rowId, row);
    if (this.isSnapshotComplete) {
      this.dispatchUpdate();
    }
  }

  onRowUpdated(rowId, row){
    super.onRowUpdated(rowId, row);
    this.dispatchUpdate();
  }

  onRowRemoved(rowId){
    super.onRowRemoved(rowId);
    this.dispatchUpdate();
  }

  dispatchUpdate(){
    Logger.error('This class should not be used directly. Extend the class and implement this function');
  }
}
