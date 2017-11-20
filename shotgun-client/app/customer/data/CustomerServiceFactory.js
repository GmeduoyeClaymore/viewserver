import OrderItemsDao from './OrderItemsDao';
import CartItemsDao from './CartItemsDao';
import CartSummaryDao from './CartSummaryDao';
import OrderDao from './OrderDao';
import CustomerDao from './CustomerDao';
import PaymentCardsDao from './PaymentCardsDao';
import DeliveryAddressDao from './DeliveryAddressDao';
import ProductCategoryDao from './ProductCategoryDao';
import DeliveryDao from './DeliveryDao';
import ProductDao from './ProductDao';
import Dao from './DaoBase';
import {registerDao, updateSubscriptionAction} from 'common/dao';


export default class CustomerServiceFactory {
  constructor(client, dispatch) {
    this.client = client;
    this.dispatch = dispatch;
    this.register = this.register.bind(this);
  }

  register(daoContext, customerId){
    const dao = new Dao(daoContext);
    const {dispatch} = this;
    dispatch(registerDao(dao));
    dispatch(updateSubscriptionAction(dao.name, {customerId}));
    return dao;
  }
  async create(customerId){
    if (this.customerService){
      return this.customerService;
    }
    this.register(new ProductCategoryDao(this.client), customerId);
    this.register(new OrderItemsDao(this.client), customerId);
    const orderDao = this.register(new OrderDao(this.client), customerId);
    const paymentCardsDao = this.register(new PaymentCardsDao(this.client), customerId);
    const deliveryAddressDao = this.register(new DeliveryAddressDao(this.client), customerId);
    this.register(new CustomerDao(this.client, paymentCardsDao, deliveryAddressDao), customerId);
    const deliveryDao = this.register(new DeliveryDao(this.client), customerId);
    this.register(new CartSummaryDao(this.client), customerId);
    this.register(new ProductDao(this.client));
    this.register(new CartItemsDao(this.client, orderDao, deliveryDao), customerId);
  }
}

