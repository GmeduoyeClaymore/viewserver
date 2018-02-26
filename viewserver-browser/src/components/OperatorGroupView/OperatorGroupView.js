import React, { Component } from 'react';
import PagingListView from 'components/Dao/PagingListView'
import LoadingScreen from 'common-components/LoadingScreen'
import { connect } from 'react-redux';
import ViewServerGrid from 'canv-grid/ViewServerGrid';
import { withRouter } from 'react-router';
import NodeGraph from 'common-components/NodeGraph';

import { updateSubscriptionAction} from 'common/dao/DaoActions';
import {getDaoContext, getDaoCommandStatus} from 'common/dao';
import {stringify,parse} from 'query-string';
import {isEqual} from 'lodash';
//TEMP TESTING IMPORT
import {actions as groupViewActions} from 'components/OperatorGroupView/component';

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
  return {
    operatorListDaoReady : getDaoContext(state,'operatorListDao'),
    operatorContentsDaoReady : getDaoContext(state,'operatorContentsDao'),
  ...props,
  ...queryStringParams,
  ...state.OperatorGroupView
} }

const OperatorGroupView_mapDispatchToProps = (dispatch, props) => { 
  return {
    selectOperator: ({operatorGroup,operator}) => {
        const {pathname,search} = props.location;
        const queryStringParams = parse(search);
        if(operatorGroup){
          queryStringParams.operatorGroup = operatorGroup;
        }
        if(operator){
          queryStringParams.operator = operator;
        }
        const queryString = stringify(queryStringParams)
        props.history.push({pathname, search: queryString})
    },
    updateSubscription: (daoName,option) => {
      dispatch(updateSubscriptionAction(daoName,option))
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
    const {operatorGroup,operator,selectOperator} = this.props;
    this.props.checkLogin(() => this.subscribeTooperatorcontents(({operatorGroup,operator})));
  }

  onRowClick(row, {history}){
    const {path} = row;
    const {operatorGroup} = this.props;
    this.props.selectOperator({operatorGroup, operator : path});
  }

  componentWillReceiveProps(props){
    const {operatorGroup,operator} = props;
    if(!isEqual({operatorGroup,operator},{operatorGroup : this.props.operatorGroup, operator : this.props.operator})){
      this.subscribeTooperatorcontents({operatorGroup,operator});  
    }
  }

  showOperatorContents({operatorGroup,operator}){
    this.props.selectOperator({operatorGroup,operator});
  }

  subscribeTooperatorcontents({operatorGroup,operator}){
    const {updateSubscription} = this.props;
    updateSubscription("operatorListDao", {operatorName : operatorGroup})
    //updateSubscription("operatorContentsDao", {operatorName : operator})
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
  renderOperatorGraph(){
    const {context={},mode,history} = this.props;
    const {nodes, links} = this;
    return <NodeGraph nodes={nodes} links={links} height={500} width={500}/>
  }

  render(){
    const {operatorListDaoReady,operatorContentsDaoReady,operator : operatorName}  = this.props;
    const {scenarioResults = {}} = this.state;
    return <div className="flex flex-col"> 
                {operatorListDaoReady ? this.renderOperatorGraph() : null}
                <div style={{position : 'relative',flex:3}} className="flex flex-col">
                  <ViewServerGrid key="1" ref={vsg => {this.grid = vsg}} daoName="01" options={{operatorName}} />
                </div>
            </div>
  }
}
export default withRouter(connect(OperatorGroupView_mapStateToProps,OperatorGroupView_mapDispatchToProps)(OperatorGroupView));
