import variable from './../variables/platform';

export default (variables = variable) => {
  const labelTheme = {
    '.focused': {
      width: 0
    },
    fontSize: 14,
    color: variables.brandLight
  };

  return labelTheme;
};
