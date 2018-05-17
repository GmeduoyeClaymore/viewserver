import variable from './../variables/platform';

export default (variables = variable) => {
  const platform = variables.platform;

  const buttonTheme = {
    '.disabled': {
      backgroundColor: variables.btnDisabledBg,
      'NativeBase.Text': {
        color: variables.btnDisabledClr
      },
      'NativeBase.Icon': {
        color: variables.btnDisabledClr,
      },
    },

    '.active': {
      backgroundColor: variables.brandSecondary,
      borderWidth: 0
    },

    '.light': {
      backgroundColor: variables.brandPrimary,
      borderWidth: 1,
      borderColor: variables.silver,
      'NativeBase.Text': {
        fontWeight: 'normal'
      }
    },

    '.success': {
      '.bordered': {
        'NativeBase.Text': {
          color: variables.btnSuccessBg,
        },
        'NativeBase.Icon': {
          color: variables.btnSuccessBg,
        },
        'NativeBase.IconNB': {
          color: variables.btnSuccessBg,
        },
      },
      backgroundColor: variables.btnSuccessBg,
    },

    '.info': {
      '.bordered': {
        'NativeBase.Text': {
          color: variables.btnInfoBg,
        },
        'NativeBase.Icon': {
          color: variables.btnInfoBg,
        },
        'NativeBase.IconNB': {
          color: variables.btnInfoBg,
        },
      },
      backgroundColor: variables.btnInfoBg,
    },

    '.warning': {
      '.bordered': {
        'NativeBase.Text': {
          color: variables.btnWarningBg,
        },
        'NativeBase.Icon': {
          color: variables.btnWarningBg,
        },
        'NativeBase.IconNB': {
          color: variables.btnWarningBg,
        },
      },
      backgroundColor: variables.btnWarningBg,
    },

    '.danger': {
      'NativeBase.Text': {
        color: variables.brandPrimary,
      },
      backgroundColor: variables.brandDanger,
    },
    '.accept': {
      'NativeBase.Text': {
        color: variables.brandPrimary,
      },
      backgroundColor: variables.brandSuccess,
    },
    '.reject': {
      'NativeBase.Text': {
        color: variables.brandPrimary,
      },
      backgroundColor: variables.brandWarning,
    },
    '.track': {
      'NativeBase.Text': {
        color: variables.brandPrimary,
      },
      backgroundColor: variables.darkGreen,
    },

    '.transparent': {
      backgroundColor: 'transparent',
      elevation: 0,
      shadowColor: null,
      shadowOffset: null,
      shadowRadius: null,
      shadowOpacity: null,

      'NativeBase.Text': {
        color: variables.brandDark,
      },
      'NativeBase.Icon': {
        color: variables.brandDark,
      },
      'NativeBase.IconNB': {
        color: variables.brandDark,
      },
      '.dark': {
        'NativeBase.Text': {
          color: variables.brandDark,
        },
        'NativeBase.IconNB': {
          color: variables.brandDark,
        },
        'NativeBase.Icon': {
          color: variables.brandDark,
        },
        backgroundColor: null,
      },
      '.danger': {
        'NativeBase.Text': {
          color: variables.btnDangerBg,
        },
        'NativeBase.IconNB': {
          color: variables.btnDangerBg,
        },
        'NativeBase.Icon': {
          color: variables.btnDangerBg,
        },
        backgroundColor: null,
      },
      '.warning': {
        'NativeBase.Text': {
          color: variables.btnWarningBg,
        },
        'NativeBase.IconNB': {
          color: variables.btnWarningBg,
        },
        'NativeBase.Icon': {
          color: variables.btnWarningBg,
        },
        backgroundColor: null,
      },
      '.info': {
        'NativeBase.Text': {
          color: variables.btnInfoBg,
        },
        'NativeBase.IconNB': {
          color: variables.btnInfoBg,
        },
        'NativeBase.Icon': {
          color: variables.btnInfoBg,
        },
        backgroundColor: null,
      },
      '.primary': {
        'NativeBase.Text': {
          color: variables.btnPrimaryBg,
        },
        'NativeBase.IconNB': {
          color: variables.btnPrimaryBg,
        },
        'NativeBase.Icon': {
          color: variables.btnPrimaryBg,
        },
        backgroundColor: null,
      },
      '.success': {
        'NativeBase.Text': {
          color: variables.btnSuccessBg,
        },
        'NativeBase.IconNB': {
          color: variables.btnSuccessBg,
        },
        'NativeBase.Icon': {
          color: variables.btnSuccessBg,
        },
        backgroundColor: null,
      },
      '.light': {
        'NativeBase.Text': {
          color: variables.brandLight,
        },
        'NativeBase.IconNB': {
          color: variables.brandLight,
        },
        'NativeBase.Icon': {
          color: variables.brandLight,
        },
        backgroundColor: null,
      },
    },

    'NativeBase.Text': {
      fontFamily: variables.btnFontFamily,
      marginLeft: 0,
      marginRight: 0,
      color: variables.brandDark,
      fontSize: variables.btnTextSize,
      lineHeight: variables.btnLineHeight,
      paddingHorizontal: 16,
      backgroundColor: 'transparent',
      fontWeight: 'bold'
    },

    'NativeBase.Icon': {
      color: variables.brandDark,
      fontSize: 24,
      marginHorizontal: 16,
      paddingTop: platform === 'ios' ? 2 : undefined,
    },

    '.iconLeft': {
      'NativeBase.Text': {
        marginLeft: 0,
      },
      'NativeBase.IconNB': {
        marginRight: 0,
        marginLeft: 16,
      },
      'NativeBase.Icon': {
        marginRight: 0,
        marginLeft: 16,
      },
    },
    '.iconRight': {
      'NativeBase.Icon': {
        position: 'absolute',
        right: 0
      },
    },
    '.picker': {
      'NativeBase.Text': {
        '.note': {
          fontSize: 16,
          lineHeight: null,
        },
      },
    },
    '.fullWidth': {
      alignSelf: 'stretch',
      justifyContent: 'center'
    },
    '.padded': {
      marginLeft: variables.contentPadding,
      marginRight: variables.contentPadding
    },
    '.paddedBottom': {
      marginBottom: variables.contentPadding
    },
    '.paddedBottomLeftRight': {
      marginLeft: variables.contentPadding,
      marginRight: variables.contentPadding,
      marginBottom: variables.contentPadding
    },
    '.paddedTopBottom': {
      marginTop: variables.contentPadding,
      marginBottom: variables.contentPadding
    },
    '.large': {
      width: '100%',
      aspectRatio: 1.5,
      alignSelf: 'stretch',
      justifyContent: 'center',
      backgroundColor: variables.brandPrimary,
      borderColor: variables.silver,
      borderWidth: 1,
      borderRadius: 8,
      'NativeBase.Icon': {
        fontSize: 50
      }
    },
    '.personButton': {
      width: '100%',
      aspectRatio: 2,
      alignSelf: 'stretch',
      justifyContent: 'center',
      borderColor: variables.silver,
      borderWidth: 1,
      borderRadius: 4,
      backgroundColor: variables.brandPrimary
    },
    '.photoButton': {
      width: '100%',
      alignSelf: 'stretch',
      justifyContent: 'center',
      borderColor: variables.silver,
      borderWidth: 1,
      borderRadius: 8,
      backgroundColor: variables.brandPrimary,
      'NativeBase.Text': {
        fontSize: 18,
        fontWeight: 'bold'
      }
    },
    '.cancelButton': {
      backgroundColor: variables.brandPrimary,
      borderWidth: 1,
      borderColor: variables.silver,
      'NativeBase.Text': {
        color: variables.brandDanger,
      }
    },
    '.signOutButton': {
      backgroundColor: variables.brandPrimary,
      borderWidth: 1,
      borderColor: variables.silver,
      'NativeBase.Text': {
        color: variables.darkGreen,
      }
    },
    '.callButton': {
      backgroundColor: variables.brandPrimary,
      borderWidth: 1,
      borderColor: variables.silver,
      'NativeBase.Text': {
        color: variables.darkGreen
      },
      'NativeBase.Icon': {
        color: variables.darkGreen
      }
    },
    '.statusButtonSml': {
      backgroundColor: variables.brandPrimary,
      borderWidth: 1,
      width: 60,
      height: 60,
      borderRadius: 30,
      borderColor: variables.silver,
      'NativeBase.Text': {
        color: variables.darkGreen
      },
      'NativeBase.Icon': {
        color: variables.darkGreen
      }
    },
    '.statusButton': {
      backgroundColor: variables.brandPrimary,
      borderWidth: 1,
      borderColor: variables.silver,
      justifyContent: 'flex-start',
      'NativeBase.Text': {
        color: variables.darkGreen
      },
      'NativeBase.Icon': {
        marginRight: 15,
        color: variables.darkGreen
      }
    },

    paddingVertical: variables.buttonPadding,
    backgroundColor: variables.btnPrimaryBg,
    borderRadius: variables.borderRadiusBase,
    borderColor: variables.btnPrimaryBg,
    borderWidth: null,
    height: 45,
    alignSelf: 'flex-start',
    flexDirection: 'row',
    elevation: 0,
    alignItems: 'center',
    justifyContent: 'space-between',
  };
  return buttonTheme;
};
