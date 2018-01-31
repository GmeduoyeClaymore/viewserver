import React, { Component } from 'react';
import PropTypes from 'prop-types';

import { connectStyle } from 'native-base-shoutem-theme';

import Ionicons from 'react-native-vector-icons/Ionicons';
import { createIconSetFromIcoMoon } from 'react-native-vector-icons';
import icoMoonConfig from './shotgun-icons-config.json';
import mapPropsToStyleNames from '../mapPropsToStyleNames';
const ShotgunIcons = createIconSetFromIcoMoon(icoMoonConfig, 'shotgun', 'shotgun-icons.ttf');

class IconNB extends Component {
	static contextTypes = {
	  theme: PropTypes.object,
	};

	componentWillMount() {
	  if (this.context.theme) {
	    switch (this.context.theme['@@shoutem.theme/themeStyle'].variables.iconFamily) {
	    case 'Shotgun':
	      this.Icon = ShotgunIcons;
	      break;
	    case 'Ionicons':
	      this.Icon = Ionicons;
	      break;
	    default:
	      this.Icon = Ionicons;
	    }
	  } else {
	    this.Icon = Ionicons;
	  }
	}

	render() {
	  return <this.Icon ref={c => (this._root = c)} {...this.props} />;
	}
}

IconNB.propTypes = {
  style: PropTypes.oneOfType([PropTypes.object, PropTypes.number, PropTypes.array]),
};

const StyledIconNB = connectStyle('NativeBase.IconNB', {}, mapPropsToStyleNames)(IconNB);

export { StyledIconNB as IconNB };
