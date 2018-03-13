import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { TouchableOpacity } from 'react-native';
import IconNB from 'react-native-vector-icons/Ionicons';
import { connectStyle } from 'native-base-shoutem-theme';
import mapPropsToStyleNames from 'common/components/utils/mapPropsToStyleNames';
import variable from 'native-base-theme/variables/platform';
import computeProps from 'common/components/utils/computeProps';

const StyledIconNB = connectStyle('NativeBase.IconNB', {}, mapPropsToStyleNames)(IconNB);

class CheckBox extends Component {
	static contextTypes = {
	  theme: PropTypes.object,
	};

	getInitialStyle(variables) {
	  return {
	    checkStyle: {
	      borderColor: this.props.color ? this.props.color : variables.checkboxBgColor,
	      backgroundColor:
					this.props.checked === true
					  ? this.props.color ? this.props.color : variables.checkboxBgColor
					  : 'transparent',
	    },
	  };
	}

	prepareRootProps(variables) {
	  const defaultProps = {
	    style: this.getInitialStyle(variables).checkStyle,
	  };

	  return computeProps(this.props, defaultProps);
	}
	render() {
	  const variables = this.context.theme ? this.context.theme['@@shoutem.theme/themeStyle'].variables : variable;
	  const platformStyle = variables.platformStyle;
	  const platform = variables.platform;
	  const rootProps = this.prepareRootProps(variables);
	  return (
	    <TouchableOpacity ref={c => (this._root = c)} {...rootProps}>
	      <StyledIconNB

	        name={platform === 'ios' && platformStyle !== 'material' ? 'ios-checkmark-outline' : 'md-checkmark'}
	      />
	    </TouchableOpacity>
	  );
	}
}

CheckBox.propTypes = {
  ...TouchableOpacity.propTypes,
  style: PropTypes.oneOfType([PropTypes.object, PropTypes.number, PropTypes.array]),
  checked: PropTypes.bool,
  onPress: PropTypes.func,
};


const StyledCheckBox = connectStyle('NativeBase.CheckBox', {}, mapPropsToStyleNames)(CheckBox);

export { StyledCheckBox as CheckBox };
