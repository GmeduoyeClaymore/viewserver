import React, { Component } from 'react';
import {View, Text} from 'react-native';
import PlacesAutocomplete from './PlacesAutocomplete';

export default class PlacesInput extends Component {
  constructor (props) {
    super(props);
  }

  triggerBlur(){
    this.input.triggerBlur();
  }

  render(){
    const {onSelect, style, client, ...props} = this.props;

    return (
      <PlacesAutocomplete
        {...props}
        ref={c => {this.input = c;}}
        minLength={2}
        autoFocus={false}
        returnKeyType={'search'}
        listViewDisplayed='auto'
        fetchDetails={true}
        client={client}
        renderDescription={row => ResultDescription(row)}
        renderRow={row => ResultRow(row)} // custom description render
        onPress={(data, details = null) => onSelect(details)}
        getDefaultValue={() => this.props.value || ''}
        styles={{...styles, ...style}}
        currentLocation={false}
        debounce={200}
      />
    );
  }
}

const ResultDescription = (row) => {
  return row.structured_formatting.main_text;
};

const ResultRow = (row) => {
  return <View>
    <Text style={styles.resultMainText}>{row.structured_formatting.main_text}</Text>
    <Text style={styles.resultSubText}>{row.structured_formatting.secondary_text}</Text>
  </View>;
};

const styles = {
  textInputContainer: {
    width: '100%',
    backgroundColor: 'transparent'
  },
  textInput: {
    borderWidth: 1,
    borderRadius: 0,
    marginLeft: 0,
    marginRight: 0,
    marginTop: 0,
    marginBottom: 0,
    height: 44
  },
  listView: {
    backgroundColor: '#ffffff',
    borderWidth: 1
  },
  resultMainText: {
    fontSize: 12
  },
  resultSubText: {
    fontSize: 8
  }
};
