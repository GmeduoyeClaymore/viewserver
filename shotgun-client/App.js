import Expo from "expo";
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import Landing from "./Landing";

export default class App extends React.Component {
  constructor() {
    super();
    this.state = {
      isReady: false
    };
  }
  async componentWillMount() {
    await Expo.Font.loadAsync({
      EncodeSansCondensed: require("./assets/fonts/EncodeSansCondensed-Thin.ttf")
    });
    this.setState({ isReady: true });
  }

  render() {
    if (!this.state.isReady) {
      return <Expo.AppLoading />;
    }
    return <Landing />;
  }
}
