export default class PrincipalService {
  static customerIdKey = '@shotgun:customerId';

  static async getCustomerIdFromDevice(){
    try {
      const customerId = await AsyncStorage.getItem(CustomerService.customerIdKey);
      return customerId;
    } catch (error) {
      //TODO - error handling here
    }
  }

  static async setCustomerIdOnDevice(customerId){
    try {
      await AsyncStorage.setItem(CustomerService.customerIdKey, customerId);
    } catch (error) {
      //TODO - error handling here
    }
  }

  static async removeCustomerIdFromDevice(){
    try {
      await AsyncStorage.removeItem(CustomerService.customerIdKey);
    } catch (error) {
      //TODO - error handling here
    }
  }
}
