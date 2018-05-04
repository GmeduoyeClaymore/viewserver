import React, {Component} from 'react';
import {Button, Spinner} from 'native-base';
import { Platform } from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';
const IS_ANDROID = Platform.OS === 'android';

export class SpinnerButton extends Component {
  constructor(){
    super();
  }

  render() {
    const {busy = false} = this.props;
    const disabled = this.props.disabled || busy;
    const {style = {}, disabledStyle = {}, ...rest} = this.props;
    const normalStyle = style;

    return (
      <Button {...rest} disabled={disabled} style={disabled ? {...style, disabledStyle} : normalStyle}>
        {this.props.children}
        {busy ? <Spinner size={IS_ANDROID ? 24 : 1} color={shotgun.btnDisabledClr} style={styles.spinner}/> : null}
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
