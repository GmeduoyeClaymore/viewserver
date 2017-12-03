import React, { Component } from 'react';
import PagingListView from 'components/Dao/PagingListView'
import LoadingScreen from 'common-components/LoadingScreen'
import { connect } from 'react-redux';

const styles ={
  container: {
    backgroundColor: '#FFFFFF',
    display: 'flex',
    alignItems: 'flex-start',
  }
};

const rowView = (row) => {
  return (<div>
    {JSON.stringify(row)}
  </div>)
};

const headerView = (row) => {
  return (<div>
    header
  </div>)
};

const Settings_mapStateToProps = (state, props) => { return {
  ...props,
  ...state.OperatorGroupView
} }

const OperatorGroupView = ({context,mode}) => {
  const {daoName,dataPath,options} = context;
  return (
    mode === 'table' ? <PagingListView
          daoName={daoName}
          dataPath={dataPath}
          style={styles.container}
          rowView={rowView}
          options={options}
          paginationWaitingView={LoadingScreen}
          emptyView={NoItems}
          pageSize={10}
          headerView={headerView}/> : <div>Render operator graph</div>
  )
}
export default connect(Settings_mapStateToProps)(OperatorGroupView);
