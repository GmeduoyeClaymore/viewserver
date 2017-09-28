import Expo from "expo";
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import Landing from "./Landing";
import Client from "./viewserver-client/Client";
import Logger from "./viewserver-client/Logger";
import ProtoLoader from './viewserver-client/core/ProtoLoader';
export default class App extends React.Component {
  constructor() {
    super();
    this.state = {
      isReady: false
    };
    this.client = new Client("ws://192.168.0.20:8080/");
  }
  async componentWillMount() {
    
    try{
      await ProtoLoader.loadAll();
      Logger.debug("Mounting component !!" + ProtoLoader.Dto.AuthenticateCommandDto);
      await this.client.connect();
    }catch(error){
      Logger.error(error);
    }
    Logger.debug("Network connected !!");
    await Expo.Font.loadAsync({
      EncodeSansCondensed: require("./assets/fonts/EncodeSansCondensed-Thin.ttf")
    });
    this.setState({ isReady: true });
  }

  render() {
    if (!this.state.isReady) {
      return <Expo.AppLoading />;
    }
    return <Landing client={this.client}/>;
  }
}
