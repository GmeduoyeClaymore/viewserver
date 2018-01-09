import variable from './../variables/platform';

export default (variables = variable) => {
  const textTheme = {
    fontSize: variables.DefaultFontSize,
    fontFamily: variables.fontFamily,
    color: variables.textColor,
    '.note': {
      color: variables.brandLight,
      fontSize: variables.noteFontSize
    },
    '.time': {
      fontSize: 12,
      color: variables.coolGrey
    },
    '.grey': {
      color: variables.coolGrey
    }
  };

  return textTheme;
};
