package io.viewserver.core;

import io.viewserver.catalog.MetadataRegistry;
import io.viewserver.configurator.Configurator;
import io.viewserver.expression.IExpressionParser;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.OperatorBase;
import io.viewserver.operators.OperatorFactoryRegistry;
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.reactor.IReactor;
import jdk.nashorn.internal.runtime.PropertyMap;

/**
 * Created by Gbemiga on 20/12/17.
 */
public interface IExecutionContext {
    void submit(Runnable work, int delay);
    void register(IOperator operator);
    void tearDownOperator(IOperator operator);
    void unregister(IOperator operator);
    IReactor getReactor();
    Configurator getConfigurator();
    IExpressionParser getExpressionParser();
    MetadataRegistry getMetadataRegistry();
    void pause();
    void resume();
    int getExecutionCount();
    FunctionRegistry getFunctionRegistry();
    OperatorFactoryRegistry getOperatorFactoryRegistry();
    void commit();
    void setReactor(IReactor reactor);
    SummaryRegistry getSummaryRegistry();
}
