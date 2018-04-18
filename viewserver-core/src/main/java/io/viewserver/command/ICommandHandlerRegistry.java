package io.viewserver.command;

public interface ICommandHandlerRegistry {
    ICommandHandler get(String command);
}
