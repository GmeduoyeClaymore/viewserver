import ShoppingCartDao from './ShoppingCartDao';
import CustomerDao from './CustomerDao';

export default class CustomerServiceFactory {
  constructor(viewserverClient) {
    this.viewserverClient = viewserverClient;
  }

  async create(customerId){
    if(this.customerService){
      return this.customerService
    }

    let shoppingCartDao = new ShoppingCartDao(this.viewserverClient,customerId);
    let customerDao = new CustomerDao(this.viewserverClient,customerId);

    await Promise.all(shoppingCartDao,customerDao);

    this.customerService = new CustomerService(customerDao,shoppingCartDao);
    return this.customerService;
  }
}
class CustomerService {
  constructor(customerDao, shoppingCartDao) {
    this.customerDao = customerDao;
    this.shoppingCartDao = shoppingCartDao;
  }
}
  