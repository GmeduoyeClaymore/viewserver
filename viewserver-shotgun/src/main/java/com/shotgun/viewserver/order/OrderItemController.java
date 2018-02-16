package com.shotgun.viewserver.order;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.ShotgunTableUpdater;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.images.ImageController;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;

@Controller(name = "orderItemController")
public class OrderItemController {

    private ShotgunTableUpdater shotgunTableUpdater;
    private ImageController imageController;

    public OrderItemController(ShotgunTableUpdater shotgunTableUpdater,
                               ImageController imageController) {
        this.shotgunTableUpdater = shotgunTableUpdater;
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
        .addValue("userId", userId)
        .addValue("orderId", orderItem.getOrderId())
        .addValue("productId", orderItem.getProductId())
        .addValue("contentTypeId", orderItem.getContentTypeId())
        .addValue("notes", orderItem.getNotes())
        .addValue("imageUrl", orderItem.getImageUrl())
        .addValue("quantity", orderItem.getQuantity())
        .addValue("orderItemId", orderItem.getOrderItemId());

        shotgunTableUpdater.addOrUpdateRow(TableNames.ORDER_ITEM_TABLE_NAME, "orderItem", orderItemRecord);

        return orderItem.getOrderItemId();
    }

    private String getUserId() {
        String driverId = (String) ControllerContext.get("userId");
        if(driverId == null){
            throw new RuntimeException("Cannot find user id in controller context. Either you aren't logged in or you're doing this on a strange thread");
        }
        return driverId;
    }
}
