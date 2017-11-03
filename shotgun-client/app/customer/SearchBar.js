import React, { Component } from 'react';
import { TextInput, StyleSheet } from 'react-native';

export default class SearchBar extends Component {
  render() {
    return (
      <TextInput
        style={styles.input}
        placeholder="Search"
        onChangeText={this.props.onChange}
      />
    );
  }
}

const styles = StyleSheet.create({
  input: {
    paddingHorizontal: 8,
    backgroundColor: '#FFFFFF',
    borderRadius: 4,
  },
});

