package io.viewserver.server.components;

public interface IServerComponent{
    void start();
    default void stop() {
    }
}
