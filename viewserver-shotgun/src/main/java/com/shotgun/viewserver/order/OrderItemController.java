package com.shotgun.viewserver.order;

import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.images.ImageController;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;

@Controller(name = "orderItemController")
public class OrderItemController {

    private ImageController imageController;

    public OrderItemController(ImageController imageController) {
        this.imageController = imageController;
    }

    @ControllerAction(path = "addOrUpdateOrderItem", isSynchronous = true)
    public String addOrUpdateOrderItem(@ActionParam(name = "userId")String userId, @ActionParam(name = "orderItem")OrderItem orderItem){
        KeyedTable orderItemTable = ControllerUtils.getKeyedTable(TableNames.ORDER_ITEM_TABLE_NAME);
        String newOrderItemId = ControllerUtils.generateGuid();


        //save image if required
        if(orderItem.getImageData() != null){
            String fileName = BucketNames.orderImages + "/" + ControllerUtils.generateGuid() + ".jpg";
            String imageUrl = imageController.saveToS3(BucketNames.shotgunclientimages.name(),fileName,orderItem.getImageData());
            orderItem.setImageUrl(imageUrl);
        }

        ITableRowUpdater tableUpdater = row -> {
            if(orderItem.getOrderItemId() == null){
                row.setString("orderItemId", newOrderItemId);
            }
            row.setString("userId", userId);
            row.setString("orderId", orderItem.getOrderId());
            row.setString("productId", orderItem.getProductId());
            row.setString("notes", orderItem.getNotes());
            row.setString("imageUrl", orderItem.getImageUrl());
            row.setInt("quantity", orderItem.getQuantity());
        };

        if(orderItem.getOrderItemId() != null){
            orderItemTable.updateRow(new TableKey(orderItem.getOrderItemId()), tableUpdater);
            return orderItem.getOrderItemId();
        }else{
            orderItemTable.addRow(new TableKey(newOrderItemId), tableUpdater);
            return newOrderItemId;
        }
    }
}
