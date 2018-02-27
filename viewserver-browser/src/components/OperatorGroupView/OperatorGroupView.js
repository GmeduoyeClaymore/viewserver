import React, { Component } from 'react';
import PagingListView from 'components/Dao/PagingListView'
import LoadingScreen from 'common-components/LoadingScreen'
import { connect } from 'react-redux';
import ViewServerGrid from 'canv-grid/ViewServerGrid';
import { withRouter } from 'react-router';
import NodeGraph from 'common-components/NodeGraph';
import {getNodesAndLinksFromConnectionsAndOperators} from './operatorConfigurationUtils'
import { updateSubscriptionAction} from 'common/dao/DaoActions';
import {getDaoContext, getDaoCommandStatus, getDaoState} from 'common/dao';
import {stringify,parse} from 'query-string';
import {isEqual} from 'lodash';
//TEMP TESTING IMPORT
import {actions as groupViewActions} from 'components/OperatorGroupView/component';
import uuid from 'uuid/v1';
import Logger from 'common/Logger';


const styles ={
  container: {
    backgroundColor: '#FFFFFF',
    display: 'flex',
    alignItems: 'flex-start',
  }
};

const rowView = ({onRowClick, row,...rest}) => {
  return (<div onClick={() => onRowClick(row,rest)} key={row.rowId}>
    {JSON.stringify(row)}
  </div>)
};

const headerView = (row) => {
  return (<div>
    header
  </div>)
};
const noItems = () => {
  return (<div>
    No items
  </div>)
};

const OperatorGroupView_mapStateToProps = (state, props) => { 
  const {search} = props.location;
  const queryStringParams = parse(search);
  const operators = getDaoState(state,[], 'operatorListDao');
  const {nodes,links} = getDaoState(state,[], 'connectionsDao') || {};

  return {
      nodes,
      links,
      operatorListDaoReady : getDaoContext(state,'operatorListDao'),
      operatorContentsDaoReady : getDaoContext(state,'operatorContentsDao'),
    ...props,
    ...queryStringParams,
    ...state.OperatorGroupView
  }  
}

const OperatorGroupView_mapDispatchToProps = (dispatch, props) => { 
  return {
    selectOperator: ({operatorGroup,operator,operatorPathPrefix,operatorPathField}) => {
        const {pathname,search} = props.location;
        const queryStringParams = parse(search);
        if(operatorGroup){
          queryStringParams.operatorGroup = operatorGroup;
        }
        if(operator){
          queryStringParams.operator = operator;
        }
        if(operatorPathPrefix){
          queryStringParams.operatorPathPrefix = operatorPathPrefix;
        }
        if(operatorPathField){
          queryStringParams.operatorPathField = operatorPathField;
        }
        const queryString = stringify(queryStringParams)
        props.history.push({pathname, search: queryString})
    },
    updateSubscription: (daoName,option) => {
      dispatch(updateSubscriptionAction(daoName,option))
    },
    toggleMode: () => {
      dispatch(groupViewActions.toggleMode());
    },
    checkLogin: (continueWith) => {
      dispatch((disp, getState) => {
        const state = getState();
        const getLoginCommandStatus = getDaoCommandStatus(state, 'login', 'loginDao');
        const loggedInOrLoggingIn = getLoginCommandStatus === "success" || getLoginCommandStatus === "start";
        const {pathname,search} = props.location;
        if(!loggedInOrLoggingIn){
          props.history.push(
            {
              pathname: '/login',
              search: stringify({pathname,search})
            })
        }else{
          if(continueWith){
            continueWith();
          }
        }
      })
    }
  } 
}

class OperatorGroupView extends Component{
  constructor(props){
    super(props)
    this.state = {};
    const nodeCount = 100;
    this.nodes = [];
  }

  componentWillMount(){
    const {operatorGroup,operator,selectOperator, operatorPathPrefix, operatorPathField} = this.props;
    this.props.checkLogin(() => this.subscribeTooperatorcontents(({operatorGroup,operator, operatorPathPrefix, operatorPathField})));
  }

  onRowClick(row, {history}){
    const {operatorGroup,operatorPathField='path',operatorPathPrefix} = this.props;
    const path = row[operatorPathField];
    this.props.selectOperator({operatorGroup, operator : path, operatorPathPrefix, operatorPathField});
  }

  componentWillReceiveProps(props){
    const {operatorGroup,operator,operatorPathPrefix, operatorPathField} = props;
    if(!isEqual({operatorGroup,operator,operatorPathPrefix, operatorPathField},{operatorPathField: this.props.operatorPathField, operatorPathPrefix: this.props.operatorPathPrefix, operatorGroup : this.props.operatorGroup, operator : this.props.operator})){
      this.subscribeTooperatorcontents({operatorGroup,operator,operatorPathPrefix, operatorPathField});  
    }
  }

  showOperatorContents({operatorGroup,operator, operatorPathField, operatorPathPrefix}){
    this.props.selectOperator({operatorGroup,operator, operatorPathField, operatorPathPrefix});
  }

  subscribeTooperatorcontents({operatorGroup,operator}){
    const {updateSubscription, operatorPathPrefix = '', operatorPathField} = this.props;
    updateSubscription("operatorListDao", {operatorName : operatorGroup, operatorPathField})
    updateSubscription("connectionsDao", {operatorName : operatorGroup, operatorPathField})
    //updateSubscription("operatorContentsDao", {operatorName : operatorPathPrefix + operator})
  }

  renderOperators(){
    const {context={},mode,history} = this.props;
    return mode === 'table' ? <PagingListView
            daoName="operatorListDao"
            dataPath={[]}
            style={styles.container}
            rowView={rowView}
            history={history}
            onRowClick={this.onRowClick.bind(this)}
            paginationWaitingView={LoadingScreen}
            emptyView={noItems}
            pageSize={10}
            headerView={headerView}/> : <div>Render operator graph</div>;
  }

  renderOperatorGraph(fullScreen){
    const {context={},mode,history, nodes, links} = this.props;
    return <NodeGraph  nodes={nodes} links={links} height={fullScreen ? 800 : 400} width={1300}/>
  }

  render(){
    const {operatorListDaoReady,operatorContentsDaoReady,operator : operatorName, mode, toggleMode, operatorPathPrefix = ''}  = this.props;
    const {scenarioResults = {}} = this.state;
    return <div className="flex flex-col"> 
                <a onClick={toggleMode}>{mode === 'graph' ? 'Table' : 'Graph'}</a>
                {mode == 'graph' ?  this.renderOperatorGraph(!operatorName) : this.renderOperators()}
                {operatorName ? <div style={{position : 'relative',flex:3}} className="flex flex-col">
                  <ViewServerGrid key="1" ref={vsg => {this.grid = vsg}} daoName="01" options={{operatorName: operatorPathPrefix + '' + operatorName}} />
                </div> : null}
            </div>
  }
}
export default withRouter(connect(OperatorGroupView_mapStateToProps,OperatorGroupView_mapDispatchToProps)(OperatorGroupView));
