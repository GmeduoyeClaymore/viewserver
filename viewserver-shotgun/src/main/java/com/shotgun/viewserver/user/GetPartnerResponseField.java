package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import com.shotgun.viewserver.order.types.NegotiationResponse;
import io.viewserver.expression.function.IUserDefinedFunction;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

import static io.viewserver.core.Utils.fromArray;

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
        NegotiatedOrder order = JSONBackedObjectFactory.create(orderDetailJson, NegotiatedOrder.class);
        if(order.getResponses() == null ){
            return null;
        }
        Optional<NegotiationResponse> any = fromArray(order.getResponses()).filter(c -> c.getPartnerId().equals(userId)).findAny();
        if(!any.isPresent()){
            return null;
        }
        try {
            return ControllerUtils.toString(any.get().get(responseField));
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
    }
}



