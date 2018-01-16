package com.shotgun.viewserver.order;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.delivery.Delivery;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import java.util.Date;

@Controller(name = "orderController")
public class OrderController {

    private static String ORDER_TABLE_NAME = "/datasources/order/order";

    @ControllerAction(path = "createOrder", isSynchronous = true)
    public String createOrder(@ActionParam(name = "paymentId")String paymentId,
                              @ActionParam(name = "delivery")Delivery delivery, @ActionParam(name = "orderItems")OrderItem[] orderItems){

        String userId = (String) ControllerContext.get("userId");
        if(userId == null){
            throw new RuntimeException("User id must be set in the controller context before this method is called");
        }

        KeyedTable orderTable = ControllerUtils.getKeyedTable(ORDER_TABLE_NAME);
        DeliveryAddressController deliveryAddressController = new DeliveryAddressController();
        DeliveryController deliveryController = new DeliveryController();
        OrderItemController orderItemController = new OrderItemController();


        //add addresses
        String originDeliveryAddressId = deliveryAddressController.addOrUpdateDeliveryAddress(delivery.getOrigin());
        if(delivery.getDestination() != null && delivery.getDestination().getLine1() != null){
            String destinationDeliverAddressId  = deliveryAddressController.addOrUpdateDeliveryAddress(delivery.getDestination());
            delivery.getDestination().setDeliveryAddressId(destinationDeliverAddressId);
        }

        delivery.getOrigin().setDeliveryAddressId(originDeliveryAddressId);
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
