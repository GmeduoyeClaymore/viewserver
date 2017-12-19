import * as TableNames from 'common/constants/TableNames';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {forEach} from 'lodash';
import PrincipalService from 'common/services/PrincipalService';

export default class CustomerDaoContext{
  constructor(client, userDao, paymentDao, deliveryAddressDao, deliveryDao, orderDao, orderItemsDao, options = {}) {
    this.client = client;
    this.userDao = userDao;
    this.paymentDao = paymentDao;
    this.deliveryAddressDao = deliveryAddressDao;
    this.orderDao = orderDao;
    this.orderItemsDao = orderItemsDao;
    this.deliveryDao = deliveryDao;
    this.options = options;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 20,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      flags: undefined
    };
  }

  get name(){
    return 'customerDao';
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      customer: {
        deliveryAddresses: dataSink.rows
      }
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, TableNames.USER_TABLE_NAME, dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.userId != newOptions.userId;
  }

  transformOptions(options){
    const {userId} = options;
    if (typeof userId === 'undefined'){
      throw new Error('userId should be defined');
    }
    return {...options, filterExpression: `userId == \"${userId}\"`};
  }

  extendDao(dao){
    dao.addCustomer = async ({customer, paymentCard, deliveryAddress}) => {
      const {dataSink} = dao;
      const schema = await dataSink.waitForSchema();
      const {userId} = dao.options;
      Logger.info(`Adding customer schema is ${JSON.stringify(schema)}`);

      //TODO - tidy this up using lodash or similar
      const user = {};
      forEach(schema, value => {
        const field = value.name;
        user[field] = customer[field];
      });

      if (user.userId == undefined) {
        user.userId = userId;
      }

      const createPaymentCustomerResp = await this.paymentDao.createPaymentCustomer({email: user.email, paymentCard});

      user.stripeCustomerId = createPaymentCustomerResp.customerId;
      user.stripeDefaultSourceId = createPaymentCustomerResp.paymentToken;

      await this.userDao.addOrUpdateUser({user});
      await this.deliveryAddressDao.addOrUpdateDeliveryAddress({userId: user.userId, deliveryAddress});

      await PrincipalService.setUserIdOnDevice(user.userId);
      return user.userId;
    };

    dao.checkout = async ({orderItem, payment, delivery}) => {
      const {userId} = dao.options;
      const {origin, destination, ...restDelivery} = delivery;

      const originDeliveryAddressId = await this.deliveryAddressDao.addOrUpdateDeliveryAddress({userId, deliveryAddress: origin});
      const destinationDeliveryAddressId = destination.line1 !== undefined ? await this.deliveryAddressDao.addOrUpdateDeliveryAddress({userId, deliveryAddress: destination}) : undefined;

      const deliveryId = await this.deliveryDao.createDelivery({...restDelivery, originDeliveryAddressId, destinationDeliveryAddressId});
      const orderId = await this.orderDao.createOrder({deliveryId, paymentId: payment.paymentId});
      await this.orderItemsDao.addOrderItem({orderId, orderItem, userId});
      return orderId;
    };
  }
}

