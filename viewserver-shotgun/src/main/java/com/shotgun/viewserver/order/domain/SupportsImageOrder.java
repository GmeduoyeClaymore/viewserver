package com.shotgun.viewserver.order.domain;

import java.util.List;

import static io.viewserver.core.Utils.toList;

public interface SupportsImageOrder extends BasicOrder{
    String[] getImages();
    default void addImage(String fileName){
        List<String> images = toList(getImages());
        images.add(fileName);
        this.set("images",images);
    }
}
