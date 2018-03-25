import { Platform, View } from 'react-native';
import CustomDatePickerAndroid from './CustomDatePickerAndroid';
import CustomDatePickerIOS from './CustomDatePickerIOS';
import React, {Component} from 'react';
import moment from 'moment';
import {Button, Text} from 'native-base';
import {isEqual} from 'lodash';
const IS_ANDROID = Platform.OS === 'android';

export const DatePicker =  (IS_ANDROID ? CustomDatePickerAndroid : CustomDatePickerIOS);

const AsapCannedDateOption = {
  name: 'ASAP',
  resolver: () => moment().add(30, 'minute').toDate()
};

export default class DatePickerControl extends Component{
  constructor(props){
    super(props);
    this.state = {};
    this.setDate = this.setDate.bind(this);
    this.setCannedDateOption = this.setCannedDateOption.bind(this);
    this.attemptToSetCannedDate = this.attemptToSetCannedDate.bind(this);
  }

  setDate(date, canned){
    this.setState({date, canned});
  }

  attemptToSetCannedDate(){
    const {state} = this;
    const {canned, date} = state;
    if (canned){
      const {resolver} = canned;
      const cannedDate = resolver(this.props);
      if (!isEqual(date, cannedDate)){
        this.setCannedDateOption(canned);
      }
    }
  }

  setCannedDateOption(canned){
    const {onConfirm} = this.props;
    const {resolver} = canned;
    const date = resolver(this.props);
    this.setDate(date, canned);
    if (onConfirm){
      onConfirm(date);
    }
  }

  render(){
    const {props, state} = this;
    const {cannedDateOptions = [AsapCannedDateOption]} = props;
    const {date} = state;
    const newProps = {...props, date};
    return <View style={{flex: 1}}>
      <DatePicker {...newProps} style={{marginRight: 15}}/>
      <View style={{marginLeft: 15, alignSelf: 'flex-end', flexDirection: 'row'}}>
        {cannedDateOptions.map( (opt) => <Button style={{marginLeft: 5, width: 70}}  key={opt.name} onPress={() => this.setCannedDateOption(opt)} >
          <Text style={{fontSize: 8}}>{opt.name}</Text>
        </Button>)}
      </View>
    </View>;
  }
}
