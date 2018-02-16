import variable from './../variables/platform';

export default (variables = variable) => {
  const checkBoxTheme = {
    '.checked': {
      'NativeBase.Icon': {
        color: variables.checkboxTickColor
      },
      'NativeBase.IconNB': {
        color: variables.checkboxTickColor
      }
    },
    
    'NativeBase.Icon': {
      color: 'transparent',
      lineHeight: variables.CheckboxIconSize,
      marginTop: variables.CheckboxIconMarginTop,
      fontSize: variables.CheckboxFontSize
    },
    'NativeBase.IconNB': {
      color: 'transparent',
      lineHeight: variables.CheckboxIconSize,
      marginTop: variables.CheckboxIconMarginTop,
      fontSize: variables.CheckboxFontSize,
      fontWeight: 'bold'
    },
    borderRadius: variables.CheckboxRadius,
    borderColor: variables.checkboxBorderColor,
    overflow: 'hidden',
    width: variables.checkboxSize,
    height: variables.checkboxSize,
    borderWidth: variables.CheckboxBorderWidth,
    paddingLeft: variables.CheckboxPaddingLeft - 1,
    paddingBottom: variables.CheckboxPaddingBottom,
    left: 10,
    '.categorySelectionCheckbox': {
      width: 40,
      height: 40,
      'NativeBase.IconNB': {
        lineHeight: 40,
        marginTop: variables.CheckboxIconMarginTop,
        marginLeft: 10,
        fontSize: 40
      },
      '.Icon': {
        lineHeight: 40,
        marginTop: variables.CheckboxIconMarginTop,
        fontSize: 40,
        size: 40
      }
    },
  };

  return checkBoxTheme;
};
