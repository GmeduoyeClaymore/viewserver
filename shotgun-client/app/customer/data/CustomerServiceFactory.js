import OrderItemsDao from './OrderItemsDao';
import CartItemsDao from './CartItemsDao';
import CartSummaryDao from './CartSummaryDao';
import OrderDao from './OrderDao';
import CustomerDao from './CustomerDao';
import PaymentCardsDao from './PaymentCardsDao';
import DeliveryAddressDao from './DeliveryAddressDao';
import DeliveryDao from './DeliveryDao';

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
    const paymentCardsDao = new PaymentCardsDao(this.viewserverClient, customerId);
    const deliveryAddressDao = new DeliveryAddressDao(this.viewserverClient, customerId);
    const deliveryDao = new DeliveryDao(this.viewserverClient);

    await Promise.all(orderItemsDao, customerDao, orderDao, cartItemsDao, cartSummaryDao, paymentCardsDao, deliveryAddressDao, deliveryDao);

    this.customerService = new CustomerService(customerId, customerDao, orderItemsDao, orderDao, cartItemsDao, cartSummaryDao, paymentCardsDao, deliveryAddressDao, deliveryDao);
    return this.customerService;
  }
}
class CustomerService {
  constructor(customerId, customerDao, orderItemsDao, orderDao, cartItemsDao, cartSummaryDao, paymentCardsDao, deliveryAddressDao, deliveryDao) {
    this.customerId = customerId;
    this.customerDao = customerDao;
    this.orderItemsDao = orderItemsDao;
    this.cartItemsDao = cartItemsDao;
    this.cartSummaryDao = cartSummaryDao;
    this.orderDao = orderDao;
    this.paymentCardsDao = paymentCardsDao;
    this.deliveryAddressDao = deliveryAddressDao;
    this.deliveryDao = deliveryDao;
  }
}
  
