import variable from './../variables/platform';

export default (variables = variable) => {
  const iconTheme = {
    fontSize: variables.iconFontSize,
    color: '#000000',
    '.paddedIcon': {
      marginRight: 10,
      marginTop: 2
    },
    '.originPin': {
      color: variables.blue
    },
    '.right': {
      position: 'absolute',
      right: 0
    },
  };

  return iconTheme;
};
