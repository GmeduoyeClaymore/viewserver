import { Platform, View } from 'react-native';
import CustomDatePickerAndroid from './CustomDatePickerAndroid';
import CustomDatePickerIOS from './CustomDatePickerIOS';
import React, {Component} from 'react';
import moment from 'moment';
import {Button, Text} from 'native-base';
const IS_ANDROID = Platform.OS === 'android';

export const DatePicker =  (IS_ANDROID ? CustomDatePickerAndroid : CustomDatePickerIOS);

export default class DatePickerControl extends Component{
  constructor(props){
    super(props);
    this.state = {};
    this.setDate = this.setDate.bind(this);
    this.setAsap = this.setAsap.bind(this);
  }

  setDate(date){
    this.setState({date});
  }

  setAsap(){
    const {onConfirm} = this.props;
    const asapDate = moment().add(30, 'minute').toDate();
    this.setDate();
    if (onConfirm){
      onConfirm(asapDate);
    }
  }

  render(){
    const {props, state} = this;
    const {hideAsap} = props;
    const {date} = state;
    const newProps = {...props, date};
    return <View>
      <DatePicker {...newProps}/>
      {hideAsap ? null : <Button onPress={this.setAsap} style={{height: 18, marginLeft: 15}}>
        <Text>ASAP</Text>
      </Button>}
    </View>;
  }
}
