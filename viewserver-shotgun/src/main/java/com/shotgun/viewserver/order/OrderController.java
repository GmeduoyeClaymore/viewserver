package com.shotgun.viewserver.order;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.setup.datasource.RatingDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller(name = "orderController")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    DeliveryAddressController deliveryAddressController;
    DeliveryController deliveryController;
    OrderItemController orderItemController;
    private PricingStrategyResolver pricingStrategyResolver;
    private IMessagingController messagingController;
    private KeyedTable productTable;
    private IDatabaseUpdater iDatabaseUpdater;
    private Integer labourerRate;

    public OrderController(IDatabaseUpdater iDatabaseUpdater,
                           DeliveryAddressController deliveryAddressController,
                           DeliveryController deliveryController,
                           OrderItemController orderItemController,
                           PricingStrategyResolver pricingStrategyResolver,
                           IMessagingController messagingController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.deliveryAddressController = deliveryAddressController;
        this.deliveryController = deliveryController;
        this.orderItemController = orderItemController;
        this.pricingStrategyResolver = pricingStrategyResolver;
        this.messagingController = messagingController;
    }

    KeyedTable getProductTable() {
        if (this.productTable == null) {
            this.productTable = ControllerUtils.getKeyedTable(TableNames.PRODUCT_TABLE_NAME);
        }
        return this.productTable;
    }


    @ControllerAction(path = "addCustomerRating", isSynchronous = true)
    public void addCustomerRating(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "rating") int rating) {
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();

        Record ratingRecord = new Record()
                .addValue("orderId", orderId)
                .addValue("userId", orderUserId)
                .addValue("rating", rating);

        iDatabaseUpdater.addOrUpdateRow(TableNames.RATING_TABLE_NAME, RatingDataSource.getDataSource().getSchema(), ratingRecord);
    }

    @ControllerAction(path = "addPartnerRating", isSynchronous = true)
    public void addDriverRating(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "rating") int rating) {
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);

        String deliveryId = (String) ControllerUtils.getColumnValue(orderTable, "deliveryId", orderId);
        String driverId = (String) ControllerUtils.getColumnValue(deliveryTable, "driverId", deliveryId);

        Record ratingRecord = new Record()
                .addValue("orderId", orderId)
                .addValue("userId", driverId)
                .addValue("rating", rating);

        iDatabaseUpdater.addOrUpdateRow(TableNames.RATING_TABLE_NAME, RatingDataSource.getDataSource().getSchema(), ratingRecord);
    }

    private int getQuantity(OrderItem orderItem) {
        return orderItem.getQuantity() == 0 ? 1 : orderItem.getQuantity();
    }

    private Product getProduct(String productId) {
        int row = this.getProductTable().getRow(new TableKey(productId));
        if (row == -1) {
            throw new RuntimeException(String.format("Unable to find product id \"%s\" in the product table", productId));
        }

        Product result = new Product();
        result.setCategoryId((String) ControllerUtils.getColumnValue(this.productTable, "categoryId", row));
        result.setDescription((String) ControllerUtils.getColumnValue(this.productTable, "description", row));
        result.setName((String) ControllerUtils.getColumnValue(this.productTable, "name", row));
        result.setPrice((int) ControllerUtils.getColumnValue(this.productTable, "price", row));
        result.setProductId((String) ControllerUtils.getColumnValue(this.productTable, "productId", row));
        return result;

    }
}
