import React, {Component} from 'react';
import { Button} from 'native-base';
import {PropTypes} from 'prop-types';
import ValidationService from '../../common/services/ValidationService';


export default class ValidatingButton extends Component {
  constructor(){
    super();
    this.state = {isValid: false};
  }

  componentWillReceiveProps(nextProps){
    this.validate(nextProps.model);
  }

  async validate(model){
    const result = await ValidationService.validate(model, this.props.validationSchema);
    this.setState({isValid: result.error == ''});
  }

  render() {
    return (
      <Button {...this.props} disabled={!this.state.isValid}>
        {this.props.children}
      </Button>
    );
  }
}

ValidatingButton.propTypes = {
  validationSchema: PropTypes.object.isRequired,
  model: PropTypes.object.isRequired
};
