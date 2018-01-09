import React, {Component} from 'react';
import {connect} from 'react-redux';
import {Text} from 'native-base';

class DriverOrderInProgress extends Component{
  constructor(props) {
    super(props);
  }

  render() {
    return <Text>Map shoould be here</Text>;
  }
}

const mapStateToProps = (state, initialProps) => ({
  ...initialProps
});

export default connect(
  mapStateToProps
)(DriverOrderInProgress);

