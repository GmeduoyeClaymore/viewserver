package io.viewserver.server.components;

import rx.Observable;

public interface IServerComponent{
    Observable start();
    default void stop() {
    }
}
