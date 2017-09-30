import React,{PropTypes} from 'react';
import { StyleSheet, Text, View } from 'react-native';
import {$} from './viewserver-client/core/JQueryish';
import Logger from './viewserver-client/Logger';

class DataSink {
  constructor(){
    this.schema = {};
    this.onSnapshotComplete = this.onSnapshotComplete.bind(this);
    this.onDataReset = this.onDataReset.bind(this);
    this.onTotalRowCount = this.onTotalRowCount.bind(this);
    this.onSchemaReset = this.onSchemaReset.bind(this);
    this.onRowAdded = this.onRowAdded.bind(this);
    this.onRowUpdated = this.onRowUpdated.bind(this);
    this.onRowRemoved = this.onRowRemoved.bind(this);
    this.onColumnAdded = this.onColumnAdded.bind(this);
  }
  onSnapshotComplete(){
    Logger.info("Snapshot complete");
  }
  onDataReset(){
    Logger.info("Data reset");
  }
  onTotalRowCount(count){
    Logger.info("total row count is " + count);
  }
  onSchemaReset(){
    this.schema = {};
  }
  onRowAdded(rowId, row){
    Logger.info("Row added - " + JSON.stringify(row));
  }
  onRowUpdated(rowId, row){
    Logger.info("Row updated - " + row);
  }
  onRowRemoved(rowId){
    Logger.info("Row removed - " + rowId);
  }
  onColumnAdded(colId, col){
    Logger.info("column added - " + col.name);
    this.schema[colId] = col;
  }
  onColumnRemoved(colId){
    delete this.schema[colId];
  }
}


export default class Landing extends React.Component {

  static propTypes = {
    client : PropTypes.object
  }

  constructor(){
    super();
    this.dataSink = new DataSink();
  }

  componentWillMount() {
    this.props.client.subscribe('/datasources/fxrates',{offset : 0,limit : 100},this.dataSink) 
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={{color: 'blue',fontFamily : 'EncodeSansCondensed'}}>Open up App.js to start working on your app!</Text>
        <Text>Changes you make will automatically reload.</Text>
        <Text>Shake your phone to open the developer menu.</Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
