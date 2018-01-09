package com.shotgun.viewserver.order;

import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.delivery.Delivery;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.images.ImageController;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;

import java.util.Date;

@Controller(name = "orderController")
public class OrderController {

    private static String ORDER_TABLE_NAME = "/datasources/order/order";

    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "userId")String userId, @ActionParam(name = "paymentId")String paymentId,
                              @ActionParam(name = "delivery")Delivery delivery, @ActionParam(name = "orderItems")OrderItem[] orderItems){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(ORDER_TABLE_NAME);
        DeliveryAddressController deliveryAddressController = new DeliveryAddressController();
        DeliveryController deliveryController = new DeliveryController();
        OrderItemController orderItemController = new OrderItemController();


        //add addresses
        String originDeliveryAddressId = deliveryAddressController.addOrUpdateDeliveryAddress(userId, delivery.getOrigin());
        String destinationDeliverAddressId = delivery.getDestination().getLine1() != null ? deliveryAddressController.addOrUpdateDeliveryAddress(userId, delivery.getDestination()) : null;
        delivery.getOrigin().setDeliveryAddressId(originDeliveryAddressId);
        delivery.getDestination().setDeliveryAddressId(destinationDeliverAddressId);

        //add delivery
        String deliveryId = deliveryController.addOrUpdateDelivery(userId, delivery);
        delivery.setDeliveryId(deliveryId);
        Date now = new Date();
        String orderId = ControllerUtils.generateGuid();

        //add order
        ITableRowUpdater tableUpdater = row -> {
            row.setString("orderId", orderId);
            row.setLong("created", now.getTime());
            row.setLong("lastModified", now.getTime());
            row.setString("status", OrderStatuses.PLACED.name());
            row.setString("userId", userId);
            row.setString("paymentId", paymentId);
            row.setString("deliveryId", deliveryId);
        };

        orderTable.addRow(new TableKey(orderId), tableUpdater);

        //add orderItems
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(orderId);
            orderItemController.addOrUpdateOrderItem(userId, orderItem);
        }

        return orderId;
    }
}
