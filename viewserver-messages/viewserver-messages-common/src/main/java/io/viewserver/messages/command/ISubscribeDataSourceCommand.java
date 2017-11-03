package io.viewserver.messages.command;

import io.viewserver.messages.IPoolableMessage;
import io.viewserver.messages.config.IProjectionConfig;

public interface ISubscribeDataSourceCommand<T> extends IPoolableMessage<T> {
    String getDataSourceName();

    ISubscribeDataSourceCommand setDataSourceName(String dataSourceName);

    String getOutputName();

    ISubscribeDataSourceCommand setOutputName(String outputName);

    IOptions getOptions();

    ISubscribeDataSourceCommand setOptions(IOptions options);

    IProjectionConfig getProjection();

    ISubscribeDataSourceCommand setProjection(IProjectionConfig projection);
}
