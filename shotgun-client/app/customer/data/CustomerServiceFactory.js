import OrderItemsDao from './OrderItemsDao';
import CartItemsDao from './CartItemsDao';
import CartSummaryDao from './CartSummaryDao';
import OrderDao from './OrderDao';
import CustomerDao from './CustomerDao';
import PaymentCardsDao from './PaymentCardsDao';
import DeliveryAddressDao from './DeliveryAddressDao';
import DeliveryDao from './DeliveryDao';
import Dao from './Dao';
import {REGISTER_DAO_ACTION, INVOKE_DAO_COMMAND} from '../../redux/DaoMiddleware';

export default class CustomerServiceFactory {
  constructor(client, dispatch) {
    this.client = client;
    this.dispatch = dispatch;
  }

  register(daoContext, customerId){
    const dao = new Dao(daoContext);
    dispatch({
      type: REGISTER_DAO_ACTION,
      dao
    });
    dispatch({
      type: INVOKE_DAO_COMMAND,
      daoName: daoContext.name,
      method: 'updatOrSubscribe',
      payload: {customerId}
    });
  }
  async create(customerId){
    if (this.customerService){
      return this.customerService;
    }

    //TODO - find a better way of passing dispatch to the dao objects
    this.register(new OrderItemsDao(this.client), customerId);
    this.register(new CartItemsDao(this.client), customerId);
    this.register(new CartSummaryDao(this.client), customerId);
    this.register(new OrderDao(this.client), customerId);
    this.register(new CustomerDao(this.client), customerId);
    this.register(new PaymentCardsDao(this.client), customerId);
    this.register(new DeliveryAddressDao(this.client), customerId);
    this.register(new DeliveryDao(this.client), customerId);
  }
}

