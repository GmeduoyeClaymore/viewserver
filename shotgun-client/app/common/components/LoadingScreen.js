import React, {Component} from 'react';
import {Container, Spinner, Text} from 'native-base';
import {PropTypes} from 'prop-types';
import {StyleSheet} from 'react-native';

export default class LoadingScreen extends Component{
  constructor(){
    super();
  }

  render() {
    const {text} = this.props;

    return <Container style={styles.container}>
      <Spinner/>
      <Text style={styles.text}>{text}</Text>
    </Container>;
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  },
  text: {
    fontSize: 10
  }
});

LoadingScreen.PropTypes = {
  text: PropTypes.number.isRequired
};
