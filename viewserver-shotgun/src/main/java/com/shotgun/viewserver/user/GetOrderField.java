package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.expression.function.IUserDefinedFunction;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class GetOrderField implements IUserDefinedFunction, IExpressionString {
    private static final Logger log = LoggerFactory.getLogger(GetPartnerResponseField.class);
    private IExpressionString orderFieldExpression;
    private IExpressionString orderDetailExpression;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 2) {
            throw new IllegalArgumentException("Syntax: gerOrderField(<fieldName (string)>,<orderdetail (json-string)>");
        }
        orderFieldExpression = (IExpressionString) parameters[0];
        orderDetailExpression = (IExpressionString) parameters[1];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.String;
    }

    @Override
    public String getString(int row) {
        String orderField = orderFieldExpression.getString(row);

        if(orderField == null || "".equals(orderField)){
            return null;
        }
        String orderDetailJson = orderDetailExpression.getString(row);
        HashMap order = ControllerUtils.mapDefault(orderDetailJson);
        if(order == null){
            return null;
        }
        try {
            return ControllerUtils.toString(order.get(orderField));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

