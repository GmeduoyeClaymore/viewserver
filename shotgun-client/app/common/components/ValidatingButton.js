import React, {Component} from 'react';
import {SpinnerButton} from 'common/components';
import {PropTypes} from 'prop-types';
import ValidationService from 'common/services/ValidationService';
import {isEqual} from 'lodash';

export class ValidatingButton extends Component {
  constructor(){
    super();
    this.state = {isValid: false, model: undefined};
  }

  async componentDidMount() {
    const {validateOnMount = false} = this.props;
    this.isMountedComponentMounted = true;
    if (validateOnMount){
      await this.validateIfRequired(this.props.model);
    }
  }

  componentWillUnmount(){
    this.isMountedComponentMounted  = false;
  }

  setState(partialState, continuewWith){
    if (this.isMountedComponentMounted){
      super.setState(partialState, continuewWith);
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
  model: PropTypes.oneOfType([PropTypes.object, PropTypes.string])
};
