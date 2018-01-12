import variable from './../variables/platform';
import { Platform, Dimensions } from 'react-native';

const deviceHeight = Dimensions.get('window').height;
export default (variables = variable) => {
  const theme = {
    '.padded': {
      paddingLeft: variables.contentPadding,
      paddingRight: variables.contentPadding
    },
    '.paddedLeft': {
      paddingLeft: variables.contentPadding,
    },
    flex: 1,
    height: Platform.OS === 'ios' ? deviceHeight : deviceHeight - 20
  };

  return theme;
};
