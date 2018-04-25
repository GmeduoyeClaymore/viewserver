package com.shotgun.viewserver.order;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.IDatabaseUpdater;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.images.ImageController;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;

import static com.shotgun.viewserver.ControllerUtils.getUserId;

@Controller(name = "orderItemController")
public class OrderItemController {

    private IDatabaseUpdater iDatabaseUpdater;
    private ImageController imageController;

    public OrderItemController(IDatabaseUpdater iDatabaseUpdater,
                               ImageController imageController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.imageController = imageController;
    }

    @ControllerAction(path = "addOrUpdateOrderItem", isSynchronous = true)
    public String addOrUpdateOrderItem(@ActionParam(name = "orderItem")OrderItem orderItem){
        String userId = getUserId();

        //save image if required
        if(orderItem.getImageData() != null){
            String fileName = BucketNames.orderImages + "/" + ControllerUtils.generateGuid() + ".jpg";
            String imageUrl = imageController.saveToS3(BucketNames.shotgunclientimages.name(),fileName,orderItem.getImageData());
            orderItem.setImageUrl(imageUrl);
        }

        if(orderItem.getOrderItemId() == null){
            orderItem.setOrderItemId(ControllerUtils.generateGuid());
        }

        Record orderItemRecord = new Record()
            .addValue("orderItemId", orderItem.getOrderItemId())
            .addValue("userId", userId)
            .addValue("orderId", orderItem.getOrderId())
            .addValue("productId", orderItem.getProductId())
            .addValue("contentTypeId", orderItem.getContentTypeId())
            .addValue("notes", orderItem.getNotes())
            .addValue("imageUrl", orderItem.getImageUrl())
            .addValue("fixedPrice", orderItem.getFixedPrice())
            .addValue("fixedPrice", orderItem.getFixedPrice())
            .addValue("fixedPrice", orderItem.getFixedPrice())
            .addValue("quantity", orderItem.getQuantity());

        if (orderItem.getStartTime() != null) {
            orderItemRecord.addValue("startTime", orderItem.getStartTime());
        }

        if (orderItem.getEndTime() != null) {
            orderItemRecord.addValue("endTime", orderItem.getEndTime());
        }

        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_ITEM_TABLE_NAME, "orderItem", orderItemRecord);

        return orderItem.getOrderItemId();
    }


}
