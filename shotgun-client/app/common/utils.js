export const addressToText = (address) => {
  if (address.line1){
    if (address.line1.startsWith('LOCATION_PLACEHOLDER_ADDRESS')){
      return 'Current Location';
    }
    return `${address.line1}, ${address.postCode}`;
  }
  return undefined;
};
