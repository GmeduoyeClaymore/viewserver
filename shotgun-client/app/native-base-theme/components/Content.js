import variable from './../variables/platform';

export default (variables = variable) => {
  const contentTheme = {
    '.padded': {
      paddingLeft: variables.contentPadding,
      paddingRight: variables.contentPadding
    },
    '.paddedLeft': {
      paddingLeft: variables.contentPadding,
    },
    flex: 1,
    backgroundColor: 'transparent',
    'NativeBase.Segment': {
      borderWidth: 0,
      backgroundColor: 'transparent'
    }
  };

  return contentTheme;
};
