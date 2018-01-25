import React, {Component} from 'react';
import SpinnerButton from 'common/components/SpinnerButton';
import {PropTypes} from 'prop-types';
import ValidationService from 'common/services/ValidationService';
import shotgun from 'native-base-theme/variables/shotgun';
import {isEqual} from 'lodash';

export default class ValidatingButton extends Component {
  constructor(){
    super();
    this.state = {isValid: false, model: undefined};
  }

  async componentDidMount() {
    const {validateOnMount = false} = this.props;

    if (validateOnMount){
      await this.validateIfRequired(this.props.model);
    }
  }

  componentWillReceiveProps(nextProps){
    this.validateIfRequired(nextProps.model);
  }

  validateIfRequired(newModel){
    const model = typeof(newModel) === 'object' ? Object.assign({}, newModel) : newModel;

    if (!isEqual(model, this.state.model)) {
      this.setState({model});
      this.validate(model);
    }
  }

  async validate(model){
    const result = await ValidationService.validate(model, this.props.validationSchema);
    this.setState({isValid: result.error == ''});
  }

  render() {
    const disabled = !this.state.isValid || this.props.disabled;

    return (
      <SpinnerButton {...this.props} disabled={disabled}>
        {this.props.children}
      </SpinnerButton>
    );
  }
}

ValidatingButton.propTypes = {
  validationSchema: PropTypes.object.isRequired,
  model: PropTypes.oneOfType([PropTypes.object, PropTypes.string]).isRequired
};
