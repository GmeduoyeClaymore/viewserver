import React, {Component} from 'react';
import {Button, Spinner} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';

export class SpinnerButton extends Component {
  constructor(){
    super();
  }

  render() {
    const {busy = false} = this.props;
    const disabled = this.props.disabled || busy;

    return (
      <Button {...this.props} disabled={disabled}>
        {this.props.children}
        {busy ? <Spinner size={24} color={shotgun.btnDisabledClr} style={styles.spinner}/> : null}
      </Button>
    );
  }
}

const styles = {
  spinner: {
    position: 'absolute',
    right: 12,
    backgroundColor: '#D4D4D4'
  }
};
