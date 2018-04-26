package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.delivery.DeliveryOrder;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.expression.function.IUserDefinedFunction;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

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
        DeliveryOrder order = JacksonSerialiser.getInstance().deserialise(orderDetailJson, DeliveryOrder.class);
        try {
            Field field = DeliveryOrder.class.getField(orderField); //Note, this can throw an exception if the field doesn't exist.
            return ControllerUtils.toString(field.get(order));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
