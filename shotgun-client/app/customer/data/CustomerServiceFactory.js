import ShoppingCartDao from './ShoppingCartDao';
import OrderDao from './OrderDao';
import CustomerDao from './CustomerDao';

export default class CustomerServiceFactory {
  constructor(viewserverClient) {
    this.viewserverClient = viewserverClient;
  }

  async create(customerId){
    if (this.customerService){
      return this.customerService;
    }

    const shoppingCartDao = new ShoppingCartDao(this.viewserverClient, customerId);
    const orderDao = new OrderDao(this.viewserverClient, customerId);
    const customerDao = new CustomerDao(this.viewserverClient, customerId);

    await Promise.all(shoppingCartDao, customerDao, orderDao);

    this.customerService = new CustomerService(customerDao, shoppingCartDao, orderDao);
    return this.customerService;
  }
}
class CustomerService {
  constructor(customerDao, shoppingCartDao, orderDao) {
    this.customerDao = customerDao;
    this.shoppingCartDao = shoppingCartDao;
    this.orderDao = orderDao;
  }
}
  
