export const calculateTotalPrice = async ({client, orderItem, delivery }) => {
  return await client.invokeJSONCommand('orderController', 'calculateTotalPrice', {orderItems: [stripImageData(orderItem)], delivery}).timeoutWithError(10000, 'Unable to load total price after 10 seconds');
};

const stripImageData = (item) => {
  const {imageData, ...rest} = item;
  return rest;
};

