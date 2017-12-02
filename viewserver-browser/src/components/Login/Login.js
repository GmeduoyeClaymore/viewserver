import React, { Component } from 'react';
import { connect } from 'react-redux';
import config from 'electron-json-config';
import { Prompt } from 'react-router-dom';
import * as deepEqual from 'deep-equal';
import {getDaoState, isOperationPending, getOperationError} from 'common/dao';
import LoadingScreen from 'common-components/LoadingScreen'
import ErrorRegion from 'common-components/ErrorRegion' 
import ValidatingInput from 'common-components/ValidatingInput' 
import { TextInput, SelectBox, resetUID } from 'common-components/inputs';
import { login } from 'common/actions/CommonActions';
import {validationSchema}  from 'common/dao/LoginDao'

const Login = (props) => {
  return (
    <div>
      <h1>Login To ViewServer</h1>
      <LoginForm url="localhost:8081" username="foo" password="bar" {...props}/>
    </div>
  );
}

const Settings_mapStateToProps = (state, props) => { return {
  busy: isOperationPending(state, 'loginDao', 'login'),
  errors: getOperationError(state, 'loginDao', 'login'),
  ...props,
  ...state.Login
} }
const Settings_mapDispatchToProps = (dispatch) => { return {
  saveSettings: (settings) => {
    config.set('Login.username', settings.username);
    config.set('Login.password', settings.password);
    config.set('Login.url', settings.url);
    dispatch(login(settings));
  }
} }
class LoginForm extends Component {
  constructor( props ) {
    super( props );
    this.state = {
      username: props.username,
      password: props.password,
      url: props.url
    };
    this.originalState = this.state;
    this.save = this.save.bind(this);
    this.reset = this.reset.bind(this);
    resetUID();
  }
  reset(){
    this.setState( this.originalState );
  }
  save(state){
    this.originalState = state;
    this.props.saveSettings( state );
  }
  render() {
    const {busy,errors} = this.props;
    return (
      !busy ? <ErrorRegion errors={errors}><div className="box">
        <Prompt
          when={ !deepEqual( this.state, this.originalState ) }
          message="You have unsaved changes in your form. Are you sure you wish to leave?"
        />

        <header className="toolbar toolbar-header">
          <h1 className="title">Settings Login</h1>
        </header>

        <div className="padded">
          <ValidatingInput validationSchema={validationSchema} label="Username" placeholder="Username" value={this.state.username} onChange={ (value) => { this.setState({username: value}); } } />
          <ValidatingInput validationSchema={validationSchema} label="Password" placeholder="Password" value={this.state.password} onChange={ (value) => { this.setState({password: value}); } } />
          <ValidatingInput validationSchema={validationSchema} label="Url" value={this.state.url} onChange={ (value) => { this.setState({url: value}); } } options={[
            {value: "localhost:8081", label: "DEV"},
          ]} />
        </div>

        <footer className="toolbar toolbar-footer">
          <div className="toolbar-actions">
            <button className="btn btn-default" onClick={() => this.reset()}>Cancel</button>
            <button className="btn btn-primary pull-right" onClick={() => this.save(this.state)}>Login</button>
          </div>
        </footer>
      </div></ErrorRegion> : <LoadingScreen text={`Logging in user "${this.state.username}" to "${this.state.url}"`}/>
    );
  }
}
export default connect(Settings_mapStateToProps, Settings_mapDispatchToProps)(Login);
