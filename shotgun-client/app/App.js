import React from 'react';
import Client from './viewserver-client/Client';
import Logger from './viewserver-client/Logger';
import ProtoLoader from './viewserver-client/core/ProtoLoader';
import CustomerLanding from './landing/CustomerLanding';

export default class App extends React.Component {
  constructor() {
    super();
    this.state = {
      isReady: false
    };
    //TODO - for some reason localhost or loopback ip doesnt' work here
    this.client = new Client('ws://192.168.0.5:8080/');
    this.principal = {
      customerId: '2BBuxi',
    };
    this.applicationMode = 'customer';
  }

  async componentWillMount() {
    try {
      await ProtoLoader.loadAll();
      Logger.debug('Mounting component !!' + ProtoLoader.Dto.AuthenticateCommandDto);
      await this.client.connect();
    } catch (error){
      Logger.error(error);
    }
    Logger.debug('Network connected !!');
    this.setState({ isReady: true });
  }

  render() {
    if (!this.state.isReady) {
      return null;
    }
    return <CustomerLanding client={this.client} principal={this.principal}/>;
  }
}
