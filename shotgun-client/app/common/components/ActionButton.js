import React, {Component} from 'react';
import {Text, Image, StyleSheet, TouchableOpacity} from 'react-native';
import PropTypes from 'prop-types';

export default class ActionButton extends Component {
    static PropTypes = {
      action: PropTypes.func,
      icon: PropTypes.object,
      buttonText: PropTypes.string,
    };

    render(){
      const {action, icon, buttonText} = this.props;
      return <TouchableOpacity onPress={action} style={styles.action}>
        <Image source={icon} style={styles.icon} />
        <Text style={styles.actionText}>{buttonText}</Text>
      </TouchableOpacity>;
    }
}


const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    backgroundColor: '#FAFAFF',
    paddingVertical: 8
  },
  action: {
    flex: 1,
    alignItems: 'center'
  },
  actionText: {
    color: '#007AFF'
  },
  icon: {
    height: 20,
    width: 20
  }
});
