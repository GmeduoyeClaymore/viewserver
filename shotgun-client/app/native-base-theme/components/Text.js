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
    '.smallText': {
      color: variables.brandLight,
      fontSize: 12
    },
    '.bold': {
      fontWeight: 'bold'
    },
    '.time': {
      fontSize: 12,
      color: variables.coolGrey
    },
    '.grey': {
      color: variables.coolGrey
    },
    '.subTitle': {
      color: variables.brandLight
    },
    '.empty': {
      fontWeight: 'bold',
      fontSize: 18,
      textAlign: 'center',
      paddingTop: 50,
      color: variables.brandDark
    }
  };

  return textTheme;
};
