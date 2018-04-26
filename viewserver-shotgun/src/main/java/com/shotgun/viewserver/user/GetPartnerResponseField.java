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
import java.util.Optional;

public class GetPartnerResponseField implements IUserDefinedFunction, IExpressionString {
    private static final Logger log = LoggerFactory.getLogger(GetPartnerResponseField.class);
    private IExpressionString userIdExpression;
    private IExpressionString responseFieldExpression;
    private IExpressionString orderDetailExpression;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 3) {
            throw new IllegalArgumentException("Syntax: getResponseField(<partnerId (string)>,<responseField (string)>,<orderdetail (json-string)>");
        }
        userIdExpression = (IExpressionString) parameters[0];
        responseFieldExpression = (IExpressionString) parameters[1];
        orderDetailExpression = (IExpressionString) parameters[2];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.String;
    }

    @Override
    public String getString(int row) {
        String userId = userIdExpression.getString(row);
        String responseField = responseFieldExpression.getString(row);

        if(userId == null || "".equals(userId)){
            return null;
        }
        if(responseField == null || "".equals(responseField)){
            return null;
        }
        String orderDetailJson = orderDetailExpression.getString(row);
        DeliveryOrder order = JacksonSerialiser.getInstance().deserialise(orderDetailJson, DeliveryOrder.class);
        if(order.responses == null ){
            return null;
        }
        Optional<DeliveryOrder.DeliveryOrderFill> any = order.responses.stream().filter(c -> c.partnerId.equals(userId)).findAny();
        if(!any.isPresent()){
            return null;
        }
        try {
            Field field = DeliveryOrder.class.getField(responseField); //Note, this can throw an exception if the field doesn't exist.
            return ControllerUtils.toString(field.get(order));
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
    }
}



