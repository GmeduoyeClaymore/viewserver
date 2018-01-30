import React from 'react';
import { TextInput, StyleSheet } from 'react-native';

const SearchBar = ({onChange}) => {
  return <TextInput
    style={styles.input}
    placeholder="Search"
    onChangeText={onChange}
  />;
};

const styles = StyleSheet.create({
  input: {
    paddingHorizontal: 8,
    backgroundColor: '#FFFFFF',
    borderRadius: 4,
  },
});

export default SearchBar;

