import React, { Component } from 'react';
import { NavLink } from 'react-router-dom'
import { connect } from 'react-redux';
import {getDaoCommandStatus} from 'common/dao';

const Settings_mapStateToProps = (state, props) => { return {
  loggedIn:  getDaoCommandStatus(state, 'login', 'loginDao') === "success",
  ...props
} }

const Menu = ({loggedIn}) => {
  return (
    <nav className="nav-group">
      <h5 className="nav-group-title">Navigation</h5>
      {!loggedIn ? <MenuRow path="/login" label="Login" icon="icon-login" />: null}
      {loggedIn ? <MenuRow path="/logout" label="Logout" icon="icon-login" />: null}
      {loggedIn ? <MenuRow path="/dataSources" label="Data Sources" icon="icon-login" />: null}
      {loggedIn ? <MenuRow path="/reports" label="Reports" icon="icon-login" />: null}
      {loggedIn ? <MenuRow path="/operators" label="Operators" icon="chart-bar" />: null}
      {loggedIn ? <MenuRow path="/sessions" label="Sessions" icon="chart-bar" />: null}
    </nav>
  );
}

const MenuRow = (props) => {
  return (
    <NavLink to={props.path} className="nav-group-item" activeClassName="active" exact={true}>
      <span className={"icon icon-" + props.icon}></span>
      {props.label}
    </NavLink>
  )
}

export default connect(Settings_mapStateToProps)(Menu);
