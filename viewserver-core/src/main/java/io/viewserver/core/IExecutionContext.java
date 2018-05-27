package io.viewserver.core;

import com.google.common.util.concurrent.ListenableFuture;
import io.viewserver.catalog.MetadataRegistry;
import io.viewserver.configurator.Configurator;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.expression.IExpressionParser;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.OperatorFactoryRegistry;
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.operators.spread.SpreadFunctionRegistry;
import io.viewserver.reactor.IReactor;

/**
 * Created by Gbemiga on 20/12/17.
 */
public interface IExecutionContext {
    ListenableFuture submit(Runnable work, int delay);
    void register(IOperator operator);
    void tearDownOperator(IOperator operator);
    void unregister(IOperator operator);

    IReactor getReactor();
    Configurator getConfigurator();
    IExpressionParser getExpressionParser();
    MetadataRegistry getMetadataRegistry();
    SpreadFunctionRegistry getSpreadColumnRegistry();
    DimensionMapper getDimensionMapper();


    void pause();
    void resume();
    int getExecutionCount();


    FunctionRegistry getFunctionRegistry();
    OperatorFactoryRegistry getOperatorFactoryRegistry();
    void commit();
    void setReactor(IReactor reactor);
    SummaryRegistry getSummaryRegistry();
}
