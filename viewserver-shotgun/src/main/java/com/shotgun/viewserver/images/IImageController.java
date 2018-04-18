package com.shotgun.viewserver.images;

import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;

public interface IImageController {
    @ControllerAction(path = "saveImage", isSynchronous = false)
    String saveImage(@ActionParam(name = "bucketName") String bucketName, @ActionParam(name = "fileName") String fileName, @ActionParam(name = "imageData") String imageData);
}
