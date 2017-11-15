import React, {Component} from 'react';
import {Input, Item, Icon} from 'native-base';
import ValidationService from '../../common/services/ValidationService';
import {PropTypes} from 'prop-types';

export default class ValidatingInput extends Component {
  constructor(){
    super();
    this.state = {touched: false, error: ''};
  }

  async onChangeText(value){
    this.formValueTouched();
    this.props.onChangeText(value);
    await this.validate(value);
  }

  async onBlur(){
    this.formValueTouched();
    await this.validate(this.props.value);
  }

  async validate(value){
    const result = await ValidationService.validate(value, this.props.validationSchema);
    this.setState({...result});
  }

  formValueTouched(){
    this.setState({touched: true});
  }

  getPlaceHolder(){
    const errorMsg = this.state.touched === true ? this.state.error : '';
    return `${this.props.placeholder} ${errorMsg}`;
  }

  render() {
    const isValid = this.state.touched === true && this.state.error === '';
    const isInvalid = this.state.touched === true &&  this.state.error !== '';

    return (
      <Item error={isInvalid} success={isValid}>
        <Input {...this.props} placeholder={this.getPlaceHolder()} onChangeText={value => this.onChangeText(value)} onBlur={() => this.onBlur()}/>
        {isValid ? <Icon name='checkmark-circle' /> : null}
        {isInvalid ? <Icon name='close-circle' /> : null}
      </Item>
    );
  }
}

ValidatingInput.propTypes = {
  validationSchema: PropTypes.object.isRequired,
  placeholder: PropTypes.string.isRequired,
};
