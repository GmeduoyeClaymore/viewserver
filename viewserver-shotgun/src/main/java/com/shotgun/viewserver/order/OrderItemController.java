package com.shotgun.viewserver.order;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.servercomponents.IDatabaseUpdater;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.images.IImageController;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;

import static com.shotgun.viewserver.ControllerUtils.getUserId;

@Controller(name = "orderItemController")
public class OrderItemController {

    private IDatabaseUpdater iDatabaseUpdater;
    private IImageController IImageController;

    public OrderItemController(IDatabaseUpdater iDatabaseUpdater,
                               IImageController IImageController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.IImageController = IImageController;
    }

    @ControllerAction(path = "addOrUpdateOrderItem", isSynchronous = true)
    public String addOrUpdateOrderItem(@ActionParam(name = "orderItem")OrderItem orderItem){
        String userId = getUserId();

        //save image if required
        if(orderItem.getImageData() != null){
            String fileName = BucketNames.orderImages + "/" + ControllerUtils.generateGuid() + ".jpg";
            String imageUrl = IImageController.saveImage(BucketNames.shotgunclientimages.name(),fileName,orderItem.getImageData());
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

        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_ITEM_TABLE_NAME, "orderItem", orderItemRecord);

        return orderItem.getOrderItemId();
    }


}
