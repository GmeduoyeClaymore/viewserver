import React, {Component} from 'react';
import { TextInput } from 'react-native';
import {debounce} from 'lodash';
const styles = {
  input: {
    paddingHorizontal: 8,
    backgroundColor: '#FFFFFF',
    borderRadius: 4,
    padding: 10,
    borderWidth: 0.5,
    borderColor: '#edeaea'
  },
};

export class SearchBar extends Component{
  constructor(props){
    super(props);
    this.doSearch = this.doSearch.bind(this);
    this.onChange = this.onChange.bind(this);
    this.doSearch = debounce(this.doSearch, 50);
    this.state = {
      text: props.text
    };
  }

  doSearch(text){
    const {onChange} = this.props;
    if (onChange){
      onChange(text);
    }
  }

  componentWillReceiveProps(){
    //this.setState({text: newProps.text });
  }

  onChange(text){
    this.doSearch(text);
    this.setState({text});
  }

  render(){
    const {style = {}, value, onChange: onChangeFromProps /* Don't get clever and remove this */, ...rest} = this.props;
    const {onChange, state} = this;
    const {text = value} = state;
    return <TextInput
      style={{...styles.input, ...style}}
      value={text}
      {...rest}
      placeholder="Search"
      onChangeText={(t) => onChange(t)}
    />;
  }
}

export default SearchBar;

