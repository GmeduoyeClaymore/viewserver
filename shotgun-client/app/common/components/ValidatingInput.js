import React, {Component} from 'react';
import {Input, Item} from 'native-base';
import ValidationService from 'common/services/ValidationService';
import {PropTypes} from 'prop-types';
import shotgun from 'native-base-theme/variables/shotgun';
import {Icon} from 'common/components';
import {setStateIfIsMounted} from 'custom-redux';

export class ValidatingInput extends Component {
  constructor(){
    super();
    this.state = {touched: false, error: ''};
    setStateIfIsMounted(this);
  }

  async componentDidMount() {
    const {validateOnMount = false} = this.props;

    if (validateOnMount){
      await this.onBlur();
    }
  }

  async componentWillReceiveProps(nextProps){
    if (nextProps.value !== this.props.value) {
      this.formValueTouched();
      await this.validate(nextProps.value);
    }
  }

  async onChangeText(value){
    this.props.onChangeText(value);
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
    const {showIcons = true, onPress, ...rest} = this.props;

    return (
      <Item error={isInvalid} success={isValid} onPress={onPress}>
        <Input {...rest} autoCorrect={false} placeholderTextColor={shotgun.silver} onFocus={onPress} onChangeText={value => this.onChangeText(value)} onBlur={() => this.onBlur()}/>
        {showIcons && isValid ? <Icon name='checkmark' /> : null}
        {showIcons && isInvalid ? <Icon name='cross' /> : null}
      </Item>
    );
  }
}

ValidatingInput.propTypes = {
  validationSchema: PropTypes.object.isRequired,
  placeholder: PropTypes.string,
};
