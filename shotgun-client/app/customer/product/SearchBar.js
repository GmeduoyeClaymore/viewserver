import React, {Component} from 'react';
import { TextInput, StyleSheet } from 'react-native';

export default class SearchBar extends Component{

  constructor(props){
    super(props);
    this.handleChange = this.handleChange.bind(this);
    this.state = {};
  }

  handleChange(text){
    this.setState({text})
    if(this.props.onChange){
      this.props.onChange(text);
    }
  }
  render(){
    const {text} = this.state; 
    return <TextInput
      style={styles.input}
      value={text}
      placeholder="Search"
      onChangeText={this.handleChange}
    />
  };
};

const styles = StyleSheet.create({
  input: {
    paddingHorizontal: 8,
    backgroundColor: '#FFFFFF',
    borderRadius: 4,
  },
});
