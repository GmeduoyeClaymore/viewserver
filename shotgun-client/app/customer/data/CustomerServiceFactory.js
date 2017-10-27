import OrderItemsDao from './OrderItemsDao';
import CartItemsDao from './CartItemsDao';
import CartSummaryDao from './CartSummaryDao';
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

    const orderItemsDao = new OrderItemsDao(this.viewserverClient, customerId);
    const cartItemsDao = new CartItemsDao(this.viewserverClient, customerId);
    const cartSummaryDao = new CartSummaryDao(this.viewserverClient, customerId);
    const orderDao = new OrderDao(this.viewserverClient, customerId);
    const customerDao = new CustomerDao(this.viewserverClient, customerId);

    await Promise.all(orderItemsDao, customerDao, orderDao, cartItemsDao, cartSummaryDao);

    this.customerService = new CustomerService(customerDao, orderItemsDao, orderDao, cartItemsDao, cartSummaryDao);
    return this.customerService;
  }
}
class CustomerService {
  constructor(customerDao, orderItemsDao, orderDao, cartItemsDao, cartSummaryDao) {
    this.customerDao = customerDao;
    this.orderItemsDao = orderItemsDao;
    this.cartItemsDao = cartItemsDao;
    this.cartSummaryDao = cartSummaryDao;
    this.orderDao = orderDao;
  }
}
  
