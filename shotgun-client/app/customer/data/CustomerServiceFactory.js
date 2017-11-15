import OrderItemsDao from './OrderItemsDao';
import CartItemsDao from './CartItemsDao';
import CartSummaryDao from './CartSummaryDao';
import OrderDao from './OrderDao';
import CustomerDao from './CustomerDao';
import PaymentCardsDao from './PaymentCardsDao';
import DeliveryAddressDao from './DeliveryAddressDao';
import DeliveryDao from './DeliveryDao';

export default class CustomerServiceFactory {
  constructor(viewserverClient, dispatch) {
    this.viewserverClient = viewserverClient;
    this.dispatch = dispatch;
  }

  async create(customerId){
    if (this.customerService){
      return this.customerService;
    }

    //TODO - find a better way of passing dispatch to the dao objects
    const orderItemsDao = new OrderItemsDao(this.viewserverClient, customerId, this.dispatch);
    const cartItemsDao = new CartItemsDao(this.viewserverClient, customerId, this.dispatch);
    const cartSummaryDao = new CartSummaryDao(this.viewserverClient, customerId, this.dispatch);
    const orderDao = new OrderDao(this.viewserverClient, customerId, this.dispatch);
    const customerDao = new CustomerDao(this.viewserverClient);
    customerDao.subscribe(customerId);
    const paymentCardsDao = new PaymentCardsDao(this.viewserverClient, customerId, this.dispatch);
    const deliveryAddressDao = new DeliveryAddressDao(this.viewserverClient, customerId, this.dispatch);
    const deliveryDao = new DeliveryDao(this.viewserverClient, this.dispatch);

    await Promise.all(orderItemsDao, customerDao, orderDao, cartItemsDao, cartSummaryDao, paymentCardsDao, deliveryAddressDao, deliveryDao);

    this.customerService = new CustomerService(customerId, customerDao, orderItemsDao, orderDao, cartItemsDao, cartSummaryDao, paymentCardsDao, deliveryAddressDao, deliveryDao);
    return this.customerService;
  }
}
class CustomerService {
  static customerIdKey = '@shotgun:customerId';

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
  
