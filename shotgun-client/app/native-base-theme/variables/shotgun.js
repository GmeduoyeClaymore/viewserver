import color from 'color';

import { Platform, Dimensions, PixelRatio } from 'react-native';

const deviceHeight = Dimensions.get('window').height;
const deviceWidth = Dimensions.get('window').width;
const platform = Platform.OS;
const platformStyle = undefined;
const isIphoneX = platform === 'ios' && deviceHeight === 812 && deviceWidth === 375;

const colors = {
  brandPrimary: '#ffffff',
  brandSecondary: '#0cf5b6',
  brandLight: '#647479',
  brandDark: '#13181a',
  brandDanger: '#ff585f',

  brandInfo: '#62B1F6',
  brandSuccess: '#5cb85c',
  brandWarning: '#f0ad4e',
  brandSidebar: '#252932',

  hairline: '#dce3e6',
  silver: '#c0c9cc',
  coolGrey: '#8c9da1',
  blue: '#1279ff',
  lightBlue: '#a5ccff',
  gold: '#ffbd24',
  darkGreen: '#017564'
};

export default {
  ...colors,

  // Text
  textColor: colors.brandDark,
  inverseTextColor: colors.brandPrimary,
  DefaultFontSize: 14,
  noteFontSize: 12,

  //Tab style
  tabsStyle: {
    tabBarUnderlineStyle: {
      backgroundColor: colors.blue,
      height: 7,
      borderRadius: 3.5
    }
  },

  platformStyle,
  platform,
  // AndroidRipple
  androidRipple: true,
  androidRippleColor: 'rgba(256, 256, 256, 0.3)',
  androidRippleColorDark: 'rgba(0, 0, 0, 0.15)',

  // Badge
  badgeBg: '#ED1727',
  badgeColor: '#fff',
  // New Variable
  badgePadding: platform === 'ios' ? 3 : 0,

  // Button
  btnFontFamily: platform === 'ios' ? 'System' : 'Roboto_medium',
  btnDisabledBg: 'rgba(181, 181, 181, 0.6)',
  btnDisabledClr: colors.coolGrey,

  // CheckBox
  CheckboxRadius: 2,
  CheckboxBorderWidth: 2,
  CheckboxPaddingLeft: 0,
  CheckboxPaddingBottom: platform === 'ios' ? 0 : 5,
  CheckboxIconSize: 13,
  CheckboxIconMarginTop: 0,
  CheckboxFontSize: 14,
  checkboxBgColor: colors.brandSecondary,
  checkboxBorderColor: colors.brandDark,
  checkboxSize: 15,
  checkboxTickColor: colors.brandDark,

  // Segment
  segmentBackgroundColor: platform === 'ios' ? '#F8F8F8' : '#3F51B5',
  segmentActiveBackgroundColor: platform === 'ios' ? '#007aff' : '#fff',
  segmentTextColor: platform === 'ios' ? '#007aff' : '#fff',
  segmentActiveTextColor: platform === 'ios' ? '#fff' : '#3F51B5',
  segmentBorderColor: platform === 'ios' ? '#007aff' : '#fff',
  segmentBorderColorMain: platform === 'ios' ? '#a7a6ab' : '#3F51B5',

  // New Variable
  get defaultTextColor() {
    return this.textColor;
  },

  get btnPrimaryBg() {
    return colors.brandSecondary;
  },
  get btnPrimaryColor() {
    return colors.brandDark;
  },
  get btnInfoBg() {
    return this.brandInfo;
  },
  get btnInfoColor() {
    return this.inverseTextColor;
  },
  get btnSuccessBg() {
    return this.brandSuccess;
  },
  get btnSuccessColor() {
    return this.inverseTextColor;
  },
  get btnDangerBg() {
    return this.brandDanger;
  },
  get btnDangerColor() {
    return this.inverseTextColor;
  },
  get btnWarningBg() {
    return this.brandWarning;
  },
  get btnWarningColor() {
    return this.inverseTextColor;
  },
  get btnTextSize() {
    return 14;
  },
  get btnTextSizeLarge() {
    return this.fontSizeBase * 1.5;
  },
  get btnTextSizeSmall() {
    return this.fontSizeBase * 0.8;
  },
  get borderRadiusLarge() {
    return this.fontSizeBase * 3.8;
  },

  buttonPadding: 6,

  get iconSizeLarge() {
    return this.iconFontSize * 1.5;
  },
  get iconSizeSmall() {
    return this.iconFontSize * 0.6;
  },

  // Card
  cardDefaultBg: '#fff',

  // Font
  fontFamily: platform === 'ios' ? 'System' : 'Roboto',
  fontSizeBase: 14,

  get fontSizeH1() {
    return 54;
  },
  get fontSizeH2() {
    return this.fontSizeBase * 1.6;
  },
  get fontSizeH3() {
    return this.fontSizeBase * 1.4;
  },

  // Footer
  footerHeight: 50,
  footerDefaultBg: this.brandPrimary,
  footerPaddingBottom: isIphoneX ? 34 : 0,

  // FooterTab
  tabBarTextColor: this.silver,
  tabBarTextSize: 16,
  activeTab: platform === this.brandPrimary,
  sTabBarActiveTextColor: this.brandDark,
  tabBarActiveTextColor: this.brandDark,
  tabActiveBgColor: this.brandPrimary,

  // Tab
  tabDefaultBg: colors.brandPrimary,
  topTabBarTextColor: colors.coolGrey,
  topTabBarActiveTextColor: colors.brandDark,
  topTabActiveBgColor: colors.brandPrimary,
  topTabBarBorderColor: colors.brandPrimary,
  topTabBarActiveBorderColor: colors.brandPrimary,

  // Header
  toolbarBtnColor: this.brandDark,
  toolbarDefaultBg: this.brandPrimary,
  toolbarHeight: platform === 'ios' ? (isIphoneX ? 88 : 64) : 56,
  toolbarIconSize: platform === 'ios' ? 20 : 22,
  toolbarSearchIconSize: platform === 'ios' ? 20 : 23,
  toolbarInputColor: this.brandDark,
  searchBarHeight: platform === 'ios' ? 30 : 40,
  toolbarInverseBg: this.brandPrimary,
  toolbarTextColor: this.brandDark,
  toolbarDefaultBorder: this.brandPrimary,
  iosStatusbar: platform === 'ios' ? 'dark-content' : 'light-content',
  get statusBarColor() {
    return color(this.toolbarDefaultBg)
      .darken(0.2)
      .hexString();
  },

  // Icon
  iconFamily: 'Shotgun',
  iconFontSize: 18,
  iconMargin: 7,
  iconHeaderSize: platform === 'ios' ? 33 : 24,

  // InputGroup
  inputFontSize: 14,
  inputBorderColor: '#D9D5DC',
  inputSuccessBorderColor: '#2b8339',
  inputErrorBorderColor: '#ed2f2f',

  get inputColor() {
    return this.textColor;
  },
  get inputColorPlaceholder() {
    return colors.brandLight;
  },

  inputGroupMarginBottom: 10,
  inputHeightBase: 40,
  inputPaddingLeft: 5,

  get inputPaddingLeftIcon() {
    return this.inputPaddingLeft * 8;
  },

  // Line Height
  btnLineHeight: 19,
  lineHeightH1: 54,
  lineHeightH2: 27,
  lineHeightH3: 22,
  iconLineHeight: platform === 'ios' ? 37 : 30,
  lineHeight: platform === 'ios' ? 20 : 24,

  // List
  listBg: '#fff',
  listBorderColor: colors.hairline,
  listDividerBg: '#f4f4f4',
  listItemHeight: 45,
  listBtnUnderlayColor: '#DDD',

  // Card
  cardBorderColor: '#ccc',

  // Changed Variable
  listItemPadding: 0,

  listNoteColor: '#808080',
  listNoteSize: 13,

  // Progress Bar
  defaultProgressColor: '#E4202D',
  inverseProgressColor: '#1A191B',

  // Radio Button
  radioBtnSize: platform === 'ios' ? 25 : 23,
  radioSelectedColorAndroid: '#3F51B5',

  // New Variable
  radioBtnLineHeight: platform === 'ios' ? 29 : 24,

  radioColor: '#7e7e7e',

  get radioSelectedColor() {
    return color(this.radioColor)
      .darken(0.2)
      .hexString();
  },

  // Spinner
  defaultSpinnerColor: '#45D56E',
  inverseSpinnerColor: '#1A191B',

  // Tabs
  tabBgColor: '#F8F8F8',
  tabFontSize: 16,
  tabTextColor: '#222222',

  // Title
  titleFontfamily: platform === 'ios' ? 'System' : 'Roboto_medium',
  titleFontSize: 30,
  titleFontColor: colors.brandDark,
  subTitleFontSize: platform === 'ios' ? 12 : 14,
  subtitleColor: colors.brandDark,


  // Other
  borderRadiusBase: platform === 'ios' ? 5 : 2,
  borderWidth: 1 / PixelRatio.getPixelSizeForLayoutSize(1),
  contentPadding: 25,

  get darkenHeader() {
    return color(this.tabBgColor)
      .darken(0.03)
      .hexString();
  },

  dropdownBg: '#000',
  dropdownLinkColor: '#414142',
  inputLineHeight: 24,
  jumbotronBg: '#C9C9CE',
  jumbotronPadding: 30,
  deviceWidth,
  deviceHeight,
  isIphoneX,

  // New Variable
  inputGroupRoundedBorderRadius: 30,
};
