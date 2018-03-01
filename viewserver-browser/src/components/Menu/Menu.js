import React, { Component } from 'react';
import { NavLink } from 'react-router-dom'
import { connect } from 'react-redux';
import {getDaoCommandStatus, getDaoState, isLoading, getLoadingError} from 'common/dao';
import { ClipLoader } from 'react-spinners';
import {actions as configurationViewActions} from 'components/OperatorConfigurationView/component';
import { withRouter } from 'react-router';

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


const Menu = ({graphStore,history, dispatch,loggedIn,dataSources,reportContexts,reportContextsLoading, reportContextsLoadingErrors, dataSourcesLoading,reportsLoading, sessions, sessionsLoading, dataSourcesLoadingErrors, reports,reportsLoadingErrors, sessionsLoadingErrors}) => {
  return (
    <nav className="nav-group">
      <h5 className="nav-group-title">Navigation</h5>
      {!loggedIn ? <MenuRow path="/login" label="Login" icon="login" />: null}
      {loggedIn ? <DataSources dispatch={dispatch} dataSources={dataSources} loading={dataSourcesLoading} loadingErrors={dataSourcesLoadingErrors} icon="star" />: null}
      {loggedIn ? <Reports  history={history} dispatch={dispatch}  graphStore={graphStore} reports={reports} loading={reportsLoading} loadingErrors={reportsLoadingErrors} icon="login" />: null}
      {loggedIn ? <ReportContexts  history={history}  dispatch={dispatch}  reportContexts={reportContexts} loading={reportContextsLoading} loadingErrors={reportContextsLoadingErrors} icon="login" />: null}
      {loggedIn ? <Sessions sessions={sessions}  loading={sessionsLoading} loadingErrors={sessionsLoadingErrors} path="/sessions" label="Sessions" icon="user" />: null}
      {loggedIn ? <Diagnostics path="/diagnostics" label="Diagnostics" icon="chart-bar" />: null}
      {loggedIn ? <MenuRow path="/logout" label="Logout" icon="login" />: null}
    </nav>
  );
} 

const DataSources = ({dataSources = [], loading, icon, loadingErrors, dispatch}) => {
  return ( <div className="nav-group-item">

  {loading ? <ClipLoader size={12}/> :   <span className={"icon icon-" + icon} title={loadingErrors} style={loadingErrors? errorStyle : undefined} ></span>}
  {"Data Sources"}
  {dataSources.map((d) => (<DataSourceLink {...{...d, dispatch}}/>))}
</div>
)};

const Diagnostics = ({icon, loadingErrors, loading}) => {
  return ( <div className="nav-group-item">

  {loading ? <ClipLoader size={12}/> :   <span className={"icon icon-" + icon} title={loadingErrors} style={loadingErrors? errorStyle : undefined} ></span>}
  {"Diagnostics"}
  <FullOperatorLink name="Connections" path="/connections"/>
  <FullOperatorLink name="Sessions" path="/sessions"/>
</div>
)};

const FullOperatorLink = ({name, path}) => (
  <NavLink to={{pathname : "/fullOperatorView", search: `operator=${path}`}} className="nav-group-item" >
    {name}
  </NavLink>
);

const DataSourceLink = ({name, path,dispatch}) => (
  <NavLink to={{pathname : "/operatorGroupView", search: `operatorGroup=${path}&operatorPathField=path`}} className="nav-group-item" >
    {name}
  </NavLink>
);

const Reports = ({reports = [], dispatch, history, loading, icon, loadingErrors,graphStore}) => (
  <div className="nav-group-item" >
  {loading ? <ClipLoader size={12}/> :   <span className={"icon icon-" + icon} title={loadingErrors} style={loadingErrors? errorStyle : undefined} ></span>}
  {"Reports"}
  {reports.map(r => <ReportsLink {...{...r,dispatch, history,graphStore}}/>)} 
</div>
);

const ReportsLink = ({dispatch, history, ...report}) => (
  <div style={{height: 25}} className="nav-group-item" onClick={
    () => {
      dispatch(configurationViewActions.showReportGraph(report));
      history.push('/operatorConfigurationView');
    }} className="nav-group-item" >
    {report.name}
  </div>
);

const ReportContexts = ({reportContexts = [], loading, icon, loadingErrors}) => (
  <div className="nav-group-item"className="nav-group-item" >
  {loading ? <ClipLoader size={12}/> :   <span className={"icon icon-" + icon} title={loadingErrors} style={loadingErrors? errorStyle : undefined} ></span>}
  {"Report Contexts"}
  {reportContexts.map(r => <ReportContextLink {...r}/>)}
</div>
);

const ReportContextLink = ({reportName : name, path,dispatch}) => (
  <NavLink to={{pathname : "/operatorGroupView", search: `operatorGroup=${path}&operatorPathField=opName&operatorPathPrefix=/graphNodes/`}} className="nav-group-item" >
    {name}
  </NavLink> 
);

const Sessions = ({sessions = [], loading, icon, loadingErrors}) => (
  <div className="nav-group-item" className="nav-group-item" >
  {loading ? <ClipLoader size={12}/> :   <span className={"icon icon-" + icon} title={loadingErrors} style={loadingErrors? errorStyle : undefined} ></span>}
  {"Sessions"}
  {sessions.map(r => <SessionsLink {...r}/>)}
</div>
);

const SessionsLink = ({sessionId,path}) => (
  <NavLink to={{pathname : "/operatorGroupView", search: `operatorGroup=${path}&operatorPathField=path&operatorPathPrefix=`}} className="nav-group-item" >
  {sessionId}
</NavLink>
);

const MenuRow = (props) => {
  return (
    <NavLink to={props.path} className="nav-group-item" >
      <span className={"icon icon-" + props.icon}></span>
      {props.label}
    </NavLink>
  )
}

export default withRouter(connect(Settings_mapStateToProps)(Menu));
