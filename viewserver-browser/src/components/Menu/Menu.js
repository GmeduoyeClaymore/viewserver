import React, { Component } from 'react';
import { NavLink } from 'react-router-dom'
import { connect } from 'react-redux';
import {getDaoCommandStatus, getDaoState, isLoading, getLoadingError} from 'common/dao';
import { ClipLoader } from 'react-spinners';
import {actions as groupViewActions} from 'components/OperatorGroupView/component';

const errorStyle = {
  color : 'red'
}

const Settings_mapStateToProps = (state, props) => { return {
  dataSources: getDaoState(state,[],'dataSourcesDao'),
  dataSourcesLoading: isLoading(state,'dataSourcesDao'),
  dataSourcesLoadingErrors: getLoadingError(state,'dataSourcesDao'),
  reports: getDaoState(state,[],'reportsDao'),
  reportsLoading: isLoading(state,'reportsDao'),
  reportsLoadingErrors: getLoadingError(state,'reportsDao'),
  sessions: getDaoState(state,[],'sessionsDao'),
  sessionsLoading: isLoading(state,'sessionsDao'),
  sessionsLoadingErrors: getLoadingError(state,'sessionsDao'),
  loggedIn:  getDaoCommandStatus(state, 'login', 'loginDao') === "success",
  ...props
} }



const selectOperatorGroup = (dispatch,daoName,options) => () => {
  dispatch(groupViewActions.selectGroup({daoName, dataPath : [], options}));
}

const Menu = ({dispatch,loggedIn,dataSources,dataSourcesLoading,reportsLoading, sessions, sessionsLoading, dataSourcesLoadingErrors, reports,reportsLoadingErrors, sessionsLoadingErrors}) => {
  return (
    <nav className="nav-group">
      <h5 className="nav-group-title">Navigation</h5>
      {!loggedIn ? <MenuRow path="/login" label="Login" icon="login" />: null}
      {loggedIn ? <DataSources dispatch={dispatch} dataSources={dataSources} loading={dataSourcesLoading} loadingErrors={dataSourcesLoadingErrors} icon="star" />: null}
      {loggedIn ? <Reports  reports={reports} loading={reportsLoading} loadingErrors={reportsLoadingErrors} icon="login" />: null}
      {loggedIn ? <Sessions sessions={sessions}  loading={sessionsLoading} loadingErrors={sessionsLoadingErrors} path="/sessions" label="Sessions" icon="user" />: null}
      {loggedIn ? <MenuRow path="/diagnostics" label="Diagnostics" icon="chart-bar" />: null}
      {loggedIn ? <MenuRow path="/logout" label="Logout" icon="login" />: null}
    </nav>
  );
}

const DataSources = ({dataSources = [], loading, icon, loadingErrors, dispatch}) => {
  return (<NavLink to="/dataSources" className="nav-group-item" activeClassName="active" exact={true}>

  {loading ? <ClipLoader size={12}/> :   <span className={"icon icon-" + icon} title={loadingErrors} style={loadingErrors? errorStyle : undefined} ></span>}
  {"Data Sources"}
  {dataSources.map((d) => (<DataSourceLink {...{...d, dispatch}}/>))}
</NavLink>
)};

const DataSourceLink = ({name,dispatch}) => (
  <NavLink to={"/operatorGroupView" + name} className="nav-group-item" activeClassName="active" exact={true} onClick={selectOperatorGroup(dispatch,"operatorListDao",{operatorName : `datataSources/${name}`})}>
    {name}
  </NavLink>
);


const Reports = ({reports = [], loading, icon, loadingErrors}) => (
  <NavLink to="/reports" className="nav-group-item" activeClassName="active" exact={true}>
  {loading ? <ClipLoader size={12}/> :   <span className={"icon icon-" + icon} title={loadingErrors} style={loadingErrors? errorStyle : undefined} ></span>}
  {"Reports"}
  {reports.map(r => <ReportsLink {...r}/>)}
</NavLink>
);

const ReportsLink = ({name}) => (
  <NavLink to={"/reports" + name} className="nav-group-item" activeClassName="active" exact={true}>
    {name}
  </NavLink>
);


const Sessions = ({sessions = [], loading, icon, loadingErrors}) => (
  <NavLink to="/sessions" className="nav-group-item" activeClassName="active" exact={true}>
  {loading ? <ClipLoader size={12}/> :   <span className={"icon icon-" + icon} title={loadingErrors} style={loadingErrors? errorStyle : undefined} ></span>}
  {"Sessions"}
  {sessions.map(r => <SessionsLink {...r}/>)}
</NavLink>
);

const SessionsLink = ({sessionId}) => (
  <NavLink to={"/sessions" + sessionId} className="nav-group-item" activeClassName="active" exact={true}>
    {sessionId}
  </NavLink>
);

const MenuRow = (props) => {
  return (
    <NavLink to={props.path} className="nav-group-item" activeClassName="active" exact={true}>
      <span className={"icon icon-" + props.icon}></span>
      {props.label}
    </NavLink>
  )
}

export default connect(Settings_mapStateToProps)(Menu);
