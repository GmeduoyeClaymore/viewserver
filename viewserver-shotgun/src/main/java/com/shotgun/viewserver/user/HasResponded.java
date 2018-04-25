package com.shotgun.viewserver.user;

import com.shotgun.viewserver.delivery.DeliveryOrder;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.expression.function.IUserDefinedFunction;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionBool;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HasResponded implements IUserDefinedFunction, IExpressionBool {
    private static final Logger log = LoggerFactory.getLogger(HasResponded.class);
    private IExpressionString userIdExpression;
    private IExpressionString orderDetailExpression;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 2) {
            throw new IllegalArgumentException("Syntax: hasResponded(<userId (string)>,<orderdetail (json-string)>");
        }
        userIdExpression = (IExpressionString) parameters[0];
        orderDetailExpression = (IExpressionString) parameters[1];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Bool;
    }

    @Override
    public boolean getBool(int row) {
        String userId = userIdExpression.getString(row);
        if(userId == null || "".equals(userId)){
            return false;
        }
        String orderDetailJson = orderDetailExpression.getString(row);
        DeliveryOrder order = JacksonSerialiser.getInstance().deserialise(orderDetailJson, DeliveryOrder.class);
        if(order.responses == null ){
            return false;
        }
        return order.responses.stream().anyMatch(c-> c.partnerId.equals(userId));
    }
}
