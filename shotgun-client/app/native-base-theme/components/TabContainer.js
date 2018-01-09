import variable from './../variables/platform';
import { Platform } from 'react-native';

export default (variables = variable) => {
  const tabContainerTheme = {
    elevation: 0,
    flex: 0,
    height: 50,
    flexDirection: 'row',
    justifyContent: 'space-around',
    borderBottomWidth: Platform.OS === 'ios' ? variables.borderWidth : 0,
    borderColor: variables.topTabBarBorderColor
  };

  return tabContainerTheme;
};
