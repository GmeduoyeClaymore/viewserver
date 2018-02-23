import React, { Component } from 'react';
import { NavLink } from 'react-router-dom'
import { connect } from 'react-redux';
import {getDaoCommandStatus, getDaoState, isLoading, getLoadingError} from 'common/dao';
import { ClipLoader } from 'react-spinners';


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
  reportContexts: getDaoState(state,[],'reportContextsDao'),
  reportContextsLoading: isLoading(state,'reportContextsDao'),
  reportContextsLoadingErrors: getLoadingError(state,'reportContextsDao'),
  sessions: getDaoState(state,[],'sessionsDao'),
  sessionsLoading: isLoading(state,'sessionsDao'),
  sessionsLoadingErrors: getLoadingError(state,'sessionsDao'),
  loggedIn:  getDaoCommandStatus(state, 'login', 'loginDao') === "success",
  ...props
} }


const Menu = ({graphStore, dispatch,loggedIn,dataSources,reportContexts,reportContextsLoading, reportContextsLoadingErrors, dataSourcesLoading,reportsLoading, sessions, sessionsLoading, dataSourcesLoadingErrors, reports,reportsLoadingErrors, sessionsLoadingErrors}) => {
  return (
    <nav className="nav-group">
      <h5 className="nav-group-title">Navigation</h5>
      {!loggedIn ? <MenuRow path="/login" label="Login" icon="login" />: null}
      {loggedIn ? <DataSources dispatch={dispatch} dataSources={dataSources} loading={dataSourcesLoading} loadingErrors={dataSourcesLoadingErrors} icon="star" />: null}
      {loggedIn ? <Reports  graphStore={graphStore} reports={reports} loading={reportsLoading} loadingErrors={reportsLoadingErrors} icon="login" />: null}
      {loggedIn ? <ReportContexts  reportContexts={reportContexts} loading={reportContextsLoading} loadingErrors={reportContextsLoadingErrors} icon="login" />: null}
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

const DataSourceLink = ({name, path,dispatch}) => (
  <NavLink to={{pathname : "/operatorGroupView", search: `operatorGroup=${path}`}} className="nav-group-item" activeClassName="active" exact={true}>
    {name}
  </NavLink>
);


const Reports = ({reports = [], loading, icon, loadingErrors,graphStore}) => (
  <NavLink to="/reports" className="nav-group-item" activeClassName="active" exact={true}>
  {loading ? <ClipLoader size={12}/> :   <span className={"icon icon-" + icon} title={loadingErrors} style={loadingErrors? errorStyle : undefined} ></span>}
  {"Reports"}
  {reports.map(r => <ReportsLink {...r} graphStore={graphStore}/>)} 
</NavLink>
);

const ReportsLink = ({graphStore, ...report}) => (
  <div style={{height: 25}} className="nav-group-item" onClick={() => graphStore.dispatch({type: 'renderReport', data: report})} className="nav-group-item" activeClassName="active" exact={true}>
    {report.name}
  </div>
);

const ReportContexts = ({reportContexts = [], loading, icon, loadingErrors}) => (
  <NavLink to="/reportContextRegistry" className="nav-group-item" activeClassName="active" exact={true}>
  {loading ? <ClipLoader size={12}/> :   <span className={"icon icon-" + icon} title={loadingErrors} style={loadingErrors? errorStyle : undefined} ></span>}
  {"Report Contexts"}
  {reportContexts.map(r => <ReportContextLink {...r}/>)}
</NavLink>
);

const ReportContextLink = ({reportName : name, path,dispatch}) => (
  <NavLink to={{pathname : "/operatorGroupView", search: `operatorGroup=${path}`}} className="nav-group-item" activeClassName="active" exact={true}>
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
