import React, { Component } from 'react';
import PagingListView from 'components/Dao/PagingListView'
import LoadingScreen from 'common-components/LoadingScreen'
import { connect } from 'react-redux';
import ViewServerGrid from 'canv-grid/ViewServerGrid';
import { withRouter } from 'react-router';
import {stringify,parse} from 'query-string';
import {isEqual} from 'lodash';
import {actions as fullViewActions} from 'components/ControllerView/component';
import uuid from 'uuid/v1';
import Logger from 'common/Logger';
import NodeGraph from 'common-components/NodeGraph';
import ErrorRegion from 'common-components/ErrorRegion';
import { updateSubscriptionAction} from 'common/dao/DaoActions';
import {getDaoContext, getDaoCommandStatus, getDaoState, getLoadingError, isOperationPending, getDaoCommandResult, getOperationError} from 'common/dao';
import JSONPretty from 'react-json-pretty';
import { invokeJSONCommand} from 'common/dao/DaoActions';
import JSONArea from 'jsonarea';


const styles = {
  container: {
    backgroundColor: '#FFFFFF',
    display: 'flex',
    alignItems: 'flex-start',
  },
  form: {
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    margin: 8,
  },
  input: {
    appearance: 'none',
    fontSize: '16px',
    margin: 2,
    padding: '8px',
    border: '1px solid grey',
    borderRadius: 2,
  },
  button: {
    fontSize: '16px',
    margin: 2,
    padding: '8px',
    border: 0,
    borderRadius: 2,
  },
};

const rowView = ({onRowClick, row,...rest}) => {
  return (<div onClick={() => onRowClick(row,rest)} key={row.rowId}>
    {"public " + row.returnType + " " + row.path + " (" + (row.sync ? "sync" : "async") + ")"}
  </div>)
};

const headerView = (row) => {
  return (<div>
  </div>)
};
const noItems = () => {
  return (<div>
    No items
  </div>)
};


const ControllerView_mapStateToProps = (state, props) => { 
  const {search} = props.location;
  const queryStringParams = parse(search);
  const controllerActions = getDaoState(state,[], 'operatorListDao') || [];
  const {actionName,controllerName} = queryStringParams;
  let selectedAction = undefined;
  let parameterJSON = undefined;
  if(actionName){
    selectedAction = controllerActions.find(c=> c.path === actionName);
    if(controllerName){
      const paramsForController = state.ControllerView[controllerName] || {};
      parameterJSON = paramsForController[actionName] || (selectedAction ? selectedAction.parameterJSON : undefined);
    }
  }

  return {
    selectedAction,
    parameterJSON,
    controllerActions,
    ...queryStringParams,
    operatorListErrors: getLoadingError(state,'operatorListDao'),
    isControllerActionPending: isOperationPending(state,'controllersDao','invokeJSONCommand'),
    controllerActionResult: getDaoCommandResult(state,'invokeJSONCommand', 'controllersDao'),
    controllerActionErrors: getOperationError(state,'controllersDao','invokeJSONCommand'),
    ...props
  }  
}

const ControllerView_mapDispatchToProps = (dispatch, props) => { 
  return {
    invokeJSONCommand: ({controller, command, payload}) => {
      dispatch(invokeJSONCommand('controllersDao', {controller, command, payload}));
    },
    updateSubscription: (daoName,option) => {
      dispatch(updateSubscriptionAction(daoName,option))
    },
    selectControllerAction: ({path: actionName}) => {
      const {pathname,search} = props.location;
      const queryStringParams = parse(search);
      if(actionName){
        queryStringParams.actionName = actionName;
      }
      const queryString = stringify(queryStringParams)
      props.history.replace({pathname, search: queryString})
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

class ControllerView extends Component{
  constructor(props){
    super(props)
    this.state = {};

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  componentWillMount(){
    
    this.props.checkLogin();
  }

  renderControllerActions(){
    const {context={},mode,history, operatorListErrors, controller} = this.props;
    return <ErrorRegion errors={operatorListErrors}><PagingListView
            daoName="operatorListDao"
            dataPath={[]}
            options={{operatorName: controller}}
            style={styles.container}
            rowView={rowView}
            history={history}
            onRowClick={this.onRowClick.bind(this)}
            paginationWaitingView={LoadingScreen}
            emptyView={noItems}
            pageSize={10}
            headerView={headerView}/></ErrorRegion>;
  }

  onRowClick(row, {history}){
    this.props.selectControllerAction(row);
  }

  componentWillMount(){
    const {controller} = this.props;
    this.props.checkLogin();
  }

  componentWillReceiveProps(props){
    const {controller, actionName} = props;
    if(controller != this.props.controller || actionName != this.props.actionName){
      this.setState({parameterJSON: undefined});
    }
    const {parameterJSON} = this.state;
    if(!parameterJSON && props.parameterJSON){
      this.setState({parameterJSON: props.parameterJSON});
    }
  }

  handleSubmit(){
  }

  handleSubmit(event) {
    event.preventDefault();
    const {controllerName,actionName} = this.props;
    const {parameterJSON} = this.state;
    this.props.invokeJSONCommand({controller : controllerName, command: actionName, payload: JSON.parse(parameterJSON)})
  }

  handleChange(event){
    this.setState({parameterJSON: JSON.stringify(event)});
  }


  tryParse(param){
    try{
      return JSON.parse(param)
    }catch(error){}
    return param
  }

  render(){
    const {selectedAction, controller, controllerName, actionName, controllerActionErrors, controllerActionResult, isControllerActionPending, parameterJSON: parameterJSONProp} = this.props;
    const {parameterJSON  = parameterJSONProp} = this.state;
    return <div className="flex flex-col"> 
              <h1 style={{height: 50}}>{controllerName}</h1>
              <div style={{flex: 1}}>
                <div className="flex flex-row"> 
                  <div style={{flex:1}}>
                  {this.renderControllerActions()}
                  </div>
                  {selectedAction ? <div style={{flex:1, overflow: 'scroll', maxHeight: 600, padding: 20, border: '1px solid grey'}}>
                    <JSONPretty id="json-pretty" json={selectedAction.parameterJSON}></JSONPretty>
                  </div> : null}
                </div>
              </div>
                {actionName ? <div style={{flex: 1}} className="flex flex-row"> 
                    <div style={{flex:1}}>
                      <form className="flex flex-col" onSubmit={this.handleSubmit} style={{flex:1, height: '100%'}}>
                      <JSONArea className="jsonArea selectable-text" value={parameterJSON ? this.tryParse(parameterJSON) : null} onChange={this.handleChange} />
                        <button style={styles.button} onClick={this.handleSubmit} disabled={isControllerActionPending}>
                            {"Invoke " + actionName}
                        </button>
                      </form>
                    </div>
                    {controllerActionResult ||  controllerActionErrors? <div style={{flex:1}}>
                      <ErrorRegion errors={controllerActionErrors}>
                        <JSONPretty id="json-pretty" json={controllerActionResult}></JSONPretty>
                      </ErrorRegion> 
                    </div> : null}
                  </div> : null }
              
            </div>
  }
}
export default withRouter(connect(ControllerView_mapStateToProps,ControllerView_mapDispatchToProps)(ControllerView));
