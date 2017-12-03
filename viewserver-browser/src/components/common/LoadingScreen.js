import React, {Component} from 'react';
import { ScaleLoader } from 'react-spinners';
import {PropTypes} from 'prop-types';

export default class LoadingScreen extends Component{
  static propTypes = {
    text: PropTypes.string
  };
  
  constructor(props){
    super(props);
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

