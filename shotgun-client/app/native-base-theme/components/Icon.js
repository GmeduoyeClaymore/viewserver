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
    '.avgStar': {
      fontSize: 12,
      padding: 2,
      color: variables.gold
    },
    '.right': {
      position: 'absolute',
      right: 0
    },
    '.next': {
      fontSize: 12
    }
  };

  return iconTheme;
};
