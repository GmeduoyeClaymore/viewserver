package io.viewserver.expression.function;

import io.viewserver.datasource.DimensionMapper;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionInt;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoEnum implements IUserDefinedFunction, IExpressionInt {
    private static final Logger log = LoggerFactory.getLogger(UnEnum.class);
    private IExpressionString dimensionColumn;
    private IExpressionString dimensionName;
    private IExpressionString dataSourceName;

    private DimensionMapper dimensionMapper;

    public DoEnum(DimensionMapper dimensionMapper) {
        this.dimensionMapper = dimensionMapper;
    }

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 3) {
            throw new IllegalArgumentException("Syntax: enum(<valueToMap (string)>,<dimensionName (String)>,<dataSourceName (String)>)");
        }

        dimensionColumn = (IExpressionString) parameters[0];
        dimensionName = (IExpressionString) parameters[1];
        dataSourceName = (IExpressionString) parameters[2];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Int;
    }


    @Override
    public int getInt(int row) {
        String dimVal = this.dimensionColumn.getString(row);
        String dimensionName = this.dimensionName.getString(row);
        String dataSourceNameString = this.dataSourceName.getString(row);
        return this.dimensionMapper.mapString(dataSourceNameString, dimensionName, dimVal);
    }
}
