package com.shotgun.viewserver.images;

import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.BucketNames;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;

public interface IImageController {
    @ControllerAction(path = "saveOrderImage", isSynchronous = false)
    default ListenableFuture saveOrderImage(@ActionParam(name = "bucketName") String bucketName, @ActionParam(name = "imageData") String imageData){
        String fileName = bucketName + "/" + ControllerUtils.generateGuid() + ".jpg";
        return  saveImage(BucketNames.shotgunclientimages.name(), fileName, imageData);
    }
    @ControllerAction(path = "saveImage", isSynchronous = false)
    ListenableFuture saveImage(@ActionParam(name = "bucketName") String bucketName, @ActionParam(name = "fileName") String fileName, @ActionParam(name = "imageData") String imageData);
}
