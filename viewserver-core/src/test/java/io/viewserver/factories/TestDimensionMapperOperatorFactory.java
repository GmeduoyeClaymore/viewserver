package io.viewserver.factories;

import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.Cardinality;
import io.viewserver.datasource.ContentType;
import io.viewserver.datasource.Dimension;
import io.viewserver.operators.IConfigurableOperator;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.dimension.DimensionMapperOperator;
import io.viewserver.operators.dimension.IDimensionMapConfig;
import io.viewserver.schema.ITableStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestDimensionMapperOperatorFactory implements ITestOperatorFactory{
    private IExecutionContext executionContext;
    private ICatalog catalog;
    private ITableStorage tableStorage;

    public TestDimensionMapperOperatorFactory(IExecutionContext executionContext, ICatalog catalog, ITableStorage tableStorage) {
        this.executionContext = executionContext;
        this.catalog = catalog;
        this.tableStorage = tableStorage;
    }

    @Override
    public String getOperatorType() {
        return "dimMap";
    }

    @Override
    public IOperator create(String operatorName, Map<String, Object> context) {
        DimensionMapperOperator dimMapOperator = new DimensionMapperOperator(operatorName, executionContext,tableStorage,catalog, executionContext.getDimensionMapper());
        dimMapOperator.configure(getConfig(context),new CommandResult());
        return dimMapOperator;
    }

    @Override
    public void configure(String operatorName, Map<String, Object> config){
        ((IConfigurableOperator)this.catalog.getOperatorByPath(operatorName)).configure(getConfig(config),new CommandResult());
    }

    private IDimensionMapConfig getConfig(Map<String, Object> config) {

        List<Dimension> result = new ArrayList<>();
        for(Object dimension : config.values()){
            String[] parts = dimension.toString().split(",");
            result.add(new Dimension(parts[0],parts[1],Cardinality.Int, ContentType.Int,true));
        }

        return new IDimensionMapConfig() {
            @Override
            public List<Dimension> getDimensions() {
                return result;
            }

            @Override
            public boolean removeInputColumns() {
                return false;
            }

            @Override
            public String getDataSourceName() {
                return "global";
            }
        };
    }

}
