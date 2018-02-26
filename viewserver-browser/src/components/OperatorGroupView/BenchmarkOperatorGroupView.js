import React, { Component } from 'react';
import PagingListView from 'components/Dao/PagingListView'
import LoadingScreen from 'common-components/LoadingScreen'
import { connect } from 'react-redux';
import ViewServerGrid from 'canv-grid/ViewServerGrid';

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
    this.runAllScenarios = this.runAllScenarios.bind(this);
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

  async runScenario(grid,index){
    const scenarios = this.scenarios();
    const scenario = scenarios[index];
    const {name,options} = scenario;
    const {scenarioResults = {}} = this.state;
    scenarioResults[name + " on grid " + grid.props.daoName] = "Pending...";
    this.setState({scenarioResults});
    const elapsed = await grid.updateOptionsAndWait(options);
    //grid.scrollRowIntoView(options.offset);
    scenarioResults[name + " on grid " +  grid.props.daoName] = elapsed.asSeconds() + " secs "
    return new Promise((res) => this.setState({scenarioResults}, () => index+1 < scenarios.length ? this.runScenario(grid,index+1) : res()));
  }

  async runAllScenarios(grid){
    this.setState({runningScenarios:true,scenarioResults:{}})
    await this.runScenario(grid,0);
    this.setState({runningScenarios:false})
  }

  async runAllScenariosForAllGrids(){
    this.setState({scenarioResults: {}},() => {
    this.runAllScenarios(this.grid);
    this.runAllScenarios(this.grid1);
    this.runAllScenarios(this.grid2);
    this.runAllScenarios(this.grid3);});
  }

  static COLUMNS = ['name','description','rating'];

  scenarios(){
    const COLUMNS = OperatorGroupView.COLUMNS;
    return  [
      {name : "Sorting by 1 column", options:{columnsToSort : [{name : COLUMNS[0], direction: "desc"}]}},
      {name : "Sorting by 2 column", options:{columnsToSort : [{name : COLUMNS[0], direction: "desc"},{name : COLUMNS[1], direction: "desc"}]}},
      {name : "Sorting by 3 column", options:{columnsToSort : [{name : COLUMNS[0], direction: "desc"},{name : COLUMNS[1], direction: "desc"},{name : COLUMNS[2], direction: "desc"}]}},
    ];
  }

  render(){
    const {operatorListDaoReady,operatorContentsDaoReady,operator : operatorName}  = this.props;
    const {scenarioResults = {}} = this.state;
    return <div className="flex flex-col"> 
                {operatorListDaoReady ? this.renderOperators() : null}
                <div className="flex-col">{Object.keys(scenarioResults).map(c=> <div key={c}>{c + " : " + scenarioResults[c]}</div>)}</div>
                  <button className="btn btn-primary pull-right" onClick={() => this.runAllScenariosForAllGrids()}>Run Scenarios</button>
                <div style={{position : 'relative',flex:3}} className="flex flex-col">
                  <ViewServerGrid key="1" ref={vsg => {this.grid = vsg}} daoName="01" options={{operatorName}} />
                  <ViewServerGrid key="2" ref={vsg => {this.grid1 = vsg}} daoName="02" options={{operatorName}} />
                  <ViewServerGrid key="3" ref={vsg => {this.grid2 = vsg}} daoName="03" options={{operatorName}} />
                  <ViewServerGrid key="4" ref={vsg => {this.grid3 = vsg}} daoName="04" options={{operatorName}} />
                </div>
            </div>
  }
}
export default connect(OperatorGroupView_mapStateToProps,OperatorGroupView_mapDispatchToProps)(OperatorGroupView);
