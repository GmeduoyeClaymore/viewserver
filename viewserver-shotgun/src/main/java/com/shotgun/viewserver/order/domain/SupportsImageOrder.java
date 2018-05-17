package com.shotgun.viewserver.order.domain;

import java.util.List;
import java.util.stream.Collectors;

import static io.viewserver.core.Utils.fromArray;
import static io.viewserver.core.Utils.toList;

public interface SupportsImageOrder extends BasicOrder{
    String[] getImages();
    default void addImage(String fileName){
        List<String> images = toList(getImages());
        images.add(fileName);
        this.set("images",images);
    }

    default void removeImage(String imageUrl){
        List<String> images = fromArray(getImages()).filter(url -> !url.equals(imageUrl)).collect(Collectors.toList());
        this.set("images",images);
    }
}
