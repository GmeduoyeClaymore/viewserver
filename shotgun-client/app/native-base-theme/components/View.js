import variable from './../variables/platform';

export default (variables = variable) => {
  const viewTheme = {
    '.padder': {
      padding: variables.contentPadding
    },
    '.column': {
      flexDirection: 'flex-col'
    }
  };

  return viewTheme;
};
