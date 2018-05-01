import variable from './../variables/platform';

export default (variables = variable) => {
  const footerTheme = {
    'NativeBase.Left': {
      'NativeBase.Button': {
        '.transparent': {
          backgroundColor: 'transparent',
          borderColor: null,
          elevation: 0,
          shadowColor: null,
          shadowOffset: null,
          shadowRadius: null,
          shadowOpacity: null
        },
        'NativeBase.Icon': {
          color: variables.topTabBarActiveTextColor
        },
        'NativeBase.IconNB': {
          color: variables.topTabBarActiveTextColor
        },
        alignSelf: null
      },
      flex: 1,
      alignSelf: 'center',
      alignItems: 'flex-start'
    },
    'NativeBase.Body': {
      flex: 1,
      alignItems: 'center',
      alignSelf: 'center',
      flexDirection: 'row',
      'NativeBase.Button': {
        alignSelf: 'center',
        '.transparent': {
          backgroundColor: 'transparent',
          borderColor: null,
          elevation: 0,
          shadowColor: null,
          shadowOffset: null,
          shadowRadius: null,
          shadowOpacity: null
        },
        '.full': {
          height: variables.footerHeight,
          paddingBottom: variables.footerPaddingBottom,
          flex: 1
        },
        'NativeBase.Icon': {
          color: variables.topTabBarActiveTextColor
        },
        'NativeBase.IconNB': {
          color: variables.topTabBarActiveTextColor
        }
      }
    },
    'NativeBase.Right': {
      'NativeBase.Button': {
        '.transparent': {
          backgroundColor: 'transparent',
          borderColor: null,
          elevation: 0,
          shadowColor: null,
          shadowOffset: null,
          shadowRadius: null,
          shadowOpacity: null
        },
        'NativeBase.Icon': {
          color: variables.topTabBarActiveTextColor
        },
        'NativeBase.IconNB': {
          color: variables.topTabBarActiveTextColor
        },
        alignSelf: null
      },
      flex: 1,
      alignSelf: 'center',
      alignItems: 'flex-end'
    },
    backgroundColor: 'white',
    flexDirection: 'row',
    justifyContent: 'center',
    borderTopWidth: 1,
    borderColor: '#cbcbcb',
    height: variables.footerHeight,
    paddingTop: 10,
    paddingBottom: variables.footerPaddingBottom,
    elevation: 0,
    left: 0,
    right: 0,
    zIndex: 5
  };
  return footerTheme;
};
