import React, {Component} from 'react';
import ValidationService from 'common/utils/ValidationService';
import { TextInput} from 'common-components/inputs';
import {PropTypes} from 'prop-types';
import Icon from 'common-components/Icon';
import Item from 'common-components/Item';

export default class ValidatingInput extends Component {
  constructor(){
    super();
    this.state = {touched: false, error: ''};
  }

  async componentDidMount() {
    const {validateOnMount = false} = this.props;

    if (validateOnMount){
      await this.onBlur();
    }
  }

  async onChange(value){
    this.formValueTouched();
    this.props.onChange(value);
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
    const {placeholder} = this.props;
    const errorMsg = this.state.touched === true ? this.state.error : '';
    return placeholder !== undefined ? `${placeholder} ${errorMsg}` : undefined;
  }

  render() {
    const isValid = this.state.touched === true && this.state.error === '';
    const isInvalid = this.state.touched === true &&  this.state.error !== '';
    const {showIcons = true,input} = this.props;
    const Input = input || TextInput;

    return (
      <Item error={isInvalid} success={isValid}>
        <Input {...this.props} onChange={value => this.onChange(value)} onBlur={() => this.onBlur()}/>
        {showIcons && isValid ? <Icon name='checkmark-circle' /> : null}
        {showIcons && isInvalid ? <Icon name='close-circle' /> : null}
      </Item>
    );
  }
}

ValidatingInput.propTypes = {
  validationSchema: PropTypes.object.isRequired,
  input: PropTypes.oneOfType([PropTypes.object, PropTypes.func]),
  placeholder: PropTypes.string,
};
