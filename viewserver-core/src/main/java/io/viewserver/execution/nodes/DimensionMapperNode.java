package io.viewserver.execution.nodes;

import io.viewserver.datasource.Dimension;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.operators.dimension.IDimensionMapConfig;

import java.util.ArrayList;
import java.util.List;

public class DimensionMapperNode  extends GraphNodeBase<DimensionMapperNode> {
    private List<Dimension> dimensions;
    private boolean removeInputColumns;
    private String dataSourceName;

    public DimensionMapperNode(){
        super();
    }

    public DimensionMapperNode(String name) {
        super(name, "DimensionMapper");
    }

    public DimensionMapperNode withDimensions(List<Dimension> dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    public DimensionMapperNode withRemoveInputColumns(boolean removeInputColumns) {
        this.removeInputColumns = removeInputColumns;
        return this;
    }

    public DimensionMapperNode withDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        return this;
    }


    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public boolean isRemoveInputColumns() {
        return removeInputColumns;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    @Override
    public Object getConfig(final ParameterHelper parameterHelper) {
        return new IDimensionMapConfig() {
            @Override
            public List<io.viewserver.datasource.Dimension> getDimensions() {
                return parameterise(dimensions,parameterHelper);
            }

            @Override
            public boolean removeInputColumns() {
                return removeInputColumns;
            }

            @Override
            public String getDataSourceName() {
                return parameterHelper.substituteParameterValues(dataSourceName);
            }
        };
    }

    private List<Dimension> parameterise(List<Dimension> dimensions, ParameterHelper parameterHelper) {
        List<Dimension> result = new ArrayList<>();
        for(Dimension dim : dimensions){
            result.add(new Dimension(parameterHelper.substituteParameterValues(dataSourceName),dim.getSourceColumnName(), dim.getCardinality(), dim.getContentType(),dim.isGlobal()));
        }
        return result;
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        return MessagePool.getInstance().get(io.viewserver.messages.config.IDimensionMapConfig.class)
                .setDimensions(parameterise(dimensions,parameterHelper))
                .setDataSourceName(parameterHelper.substituteParameterValues(dataSourceName))
                .setRemoveInputColumns(removeInputColumns);
    }


    @Override
    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        return String.format("dimensionMapper:%s%s:%s",parameterise(dimensions,parameterHelper),parameterHelper.substituteParameterValues(this.dataSourceName), this.removeInputColumns);
    }


}
