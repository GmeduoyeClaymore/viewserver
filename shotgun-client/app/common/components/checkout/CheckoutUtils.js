
export const calculatePriceToBePaid = (totalPrice, user) => {
  return totalPrice * ( (100 - user.chargePercentage) / 100);
};

const stripImageData = (item) => {
  const {imageData, ...rest} = item;
  return rest;
};

