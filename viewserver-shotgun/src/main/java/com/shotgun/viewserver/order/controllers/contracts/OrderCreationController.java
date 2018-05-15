package com.shotgun.viewserver.order.controllers.contracts;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.controller.ControllerContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.util.dynamic.Predicate2;

import java.util.Date;
import java.util.function.Consumer;

public interface OrderCreationController {

    default <T extends BasicOrder> String create(T order, String paymentMethodId,Predicate2<Record, T> beforCreate, Consumer<T> afterTransform) {

        if(order == null){
            throw new RuntimeException("Order cannot be null");
        }

        String customerId = (String) ControllerContext.get("userId");
        if (customerId == null) {
            throw new RuntimeException("User id must be set in the controller context before this method is called");
        }

        if(order.getOrderContentType() == null){
            throw new RuntimeException("Order must have a content type");
        }

        Date now = new Date();
        String orderId = ControllerUtils.generateGuid();

        order.set("orderId", orderId);
        order.set("customerUserId", customerId);

        Record orderRecord = new Record();

        if (!beforCreate.test(orderRecord, order)) {
            return null;
        }

        order.set("amountToPay", order.calculateRemainder());

        orderRecord
                .addValue("orderId", orderId)
                .addValue("paymentMethodId", paymentMethodId)
                .addValue("requiredDate", order.getRequiredDate())
                .addValue("created", now)
                .addValue("productId", order.getOrderProduct() == null ? null : order.getOrderProduct().getProductId())
                .addValue("status", order.getOrderStatus().name())
                .addValue("orderContentTypeId", order.getOrderContentType().getContentTypeId())
                .addValue("lastModified", now)
                .addValue("userId", customerId)
                .addValue("orderDetails", order);



        getDatabaseUpdater().addOrUpdateRow(TableNames.ORDER_TABLE_NAME, OrderDataSource.getDataSource().getSchema(), orderRecord);

        afterTransform.accept(order);

        return orderId;
    }


    IDatabaseUpdater getDatabaseUpdater();

}
