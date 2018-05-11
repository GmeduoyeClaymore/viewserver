package com.shotgun.viewserver.user;

import io.viewserver.expression.function.IUserDefinedFunction;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionBool;
import io.viewserver.expression.tree.IExpressionLong;
import io.viewserver.schema.column.ColumnType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsBefore implements IUserDefinedFunction, IExpressionBool {
    private static final Logger log = LoggerFactory.getLogger(IsBefore.class);
    private IExpressionLong dateFieldExpression;
    private IExpressionLong compareToExpression;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 2) {
            throw new IllegalArgumentException("Syntax: isBefore(<aDate (long)>, <compareTo (long)>)");
        }
        dateFieldExpression = (IExpressionLong) parameters[0];
        compareToExpression = (IExpressionLong) parameters[1];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Bool;
    }

    @Override
    public boolean getBool(int row) {
        Long dateMillis = dateFieldExpression.getLong(row);
        Long other = compareToExpression.getLong(row);
        if(dateMillis == null || other == null){
            return false;
        }
        return new DateTime(dateMillis).isBefore(other);
    }
}
