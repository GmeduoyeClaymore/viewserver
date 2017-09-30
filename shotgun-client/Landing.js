import React,{PropTypes} from 'react';
import { StyleSheet, Text, View } from 'react-native';
import {$} from './viewserver-client/core/JQueryish';
import Logger from './viewserver-client/Logger';

class DataSink {
  onSnapshotComplete(){
    Logger.info("Snapshot complete");
  }
  onDataReset(){
    Logger.info("Data reset");
  }
  onRowAdded(rowId, row){
    Logger.info("Row added - " + row);
  }
  onRowUpdated(rowId, row){
    Logger.info("Row updated - " + row);
  }
  onRowRemoved(rowId){
    Logger.info("Row removed - " + rowId);
  }
  onColumnAdded(colId, col){
    Logger.info("Column added  - " + colId + " " + col);
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
    this.props.client.subscribe('/datasources',{offset : 0,limit : 100},this.dataSink,)
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
