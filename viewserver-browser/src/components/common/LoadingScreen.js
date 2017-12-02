import React, {Component} from 'react';
import { ScaleLoader } from 'react-spinners';
import {PropTypes} from 'prop-types';

export default class LoadingScreen extends Component{
  constructor(){
    super();
  }

  render() {
    const {text} = this.props;

    return <div style={styles.container}>
      <ScaleLoader/>
      <div style={styles.text}>{text}</div>
    </div>;
  }
}

const styles = {
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  },
  text: {
    fontSize: 10
  }
};

LoadingScreen.PropTypes = {
  text: PropTypes.number.isRequired
};
