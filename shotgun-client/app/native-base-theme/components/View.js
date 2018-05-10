import variable from './../variables/platform';

export default (variables = variable) => {
  const viewTheme = {
    '.padder': {
      padding: variables.contentPadding
    },
    '.column': {
      flexDirection: 'column'
    },
    '.mapBubble': {
      backgroundColor: variables.brandPrimary,
      paddingTop: 2,
      paddingBottom: 2,
      paddingLeft: 4,
      paddingRight: 4,
      borderRadius: 10,
      borderColor: variables.brandSecondary,
      borderWidth: 3,
      width: 100,
      'NativeBase.Text': {
        color: variables.brandDark,
        fontSize: 10,
        fontWeight: 'bold',
        alignSelf: 'center'
      },
    },
    '.mapArrow': {
      backgroundColor: 'transparent',
      borderWidth: 8,
      borderColor: 'transparent',
      borderTopColor: variables.brandSecondary,
      alignSelf: 'center',
      marginTop: -2,
    }
  };

  return viewTheme;
};
