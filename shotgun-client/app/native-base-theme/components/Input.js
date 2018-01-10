import variable from './../variables/platform';

export default (variables = variable) => {
  const inputTheme = {
    '.multiline': {
      height: null,
    },
    '.bold': {
      fontWeight: 'bold',
      fontSize: 18
    },
    height: variables.inputHeightBase,
    color: variables.inputColor,
    paddingLeft: 0,
    paddingRight: 5,
    flex: 1,
    fontSize: variables.inputFontSize,
    lineHeight: variables.inputLineHeight,
  };

  return inputTheme;
};
