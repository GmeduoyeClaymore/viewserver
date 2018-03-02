import React, { Component } from 'react';
import PagingListView from 'components/Dao/PagingListView'
import LoadingScreen from 'common-components/LoadingScreen'
import { connect } from 'react-redux';
import ViewServerGrid from 'canv-grid/ViewServerGrid';
import { withRouter } from 'react-router';
import {getDaoContext, getDaoCommandStatus, getDaoState, getLoadingError} from 'common/dao';
import {stringify,parse} from 'query-string';
import {isEqual} from 'lodash';
//TEMP TESTING IMPORT
import {actions as fullViewActions} from 'components/FullOperatorView/component';
import uuid from 'uuid/v1';
import Logger from 'common/Logger';


const styles ={
  container: {
    backgroundColor: '#FFFFFF',
    display: 'flex',
    alignItems: 'flex-start',
  }
};


const FullOperatorView_mapStateToProps = (state, props) => { 
  const {search} = props.location;
  const queryStringParams = parse(search);

  return {
    ...props,
    ...queryStringParams,
    ...state.FullOperatorView
  }  
}

const FullOperatorView_mapDispatchToProps = (dispatch, props) => { 
  return {
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

class FullOperatorView extends Component{
  constructor(props){
    super(props)
    this.state = {};
  }

  componentWillMount(){
    const {operatorGroup,operator,selectOperator, operatorPathPrefix, operatorPathField} = this.props;
    this.props.checkLogin();
  }

  render(){
    const {operatorListDaoReady,operatorContentsDaoReady,operator : operatorName, mode, toggleMode, operatorPathPrefix = '', operatorGroup, operatorContentsErrors}  = this.props;
    const {scenarioResults = {}} = this.state;
    return <div className="flex flex-col"> 
                {operatorName ? <div style={{position : 'relative',flex:3}} className="flex flex-col">
                  <h1>{operatorName}</h1>
                  <ViewServerGrid key="1" ref={vsg => {this.grid = vsg}} daoName="01" options={{operatorName: operatorPathPrefix + '' + operatorName}} />
                </div> : null}
            </div>
  }
}
export default withRouter(connect(FullOperatorView_mapStateToProps,FullOperatorView_mapDispatchToProps)(FullOperatorView));
