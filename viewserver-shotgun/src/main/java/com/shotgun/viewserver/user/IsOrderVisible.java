package com.shotgun.viewserver.user;

import com.shotgun.viewserver.order.domain.BasicOrder;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.expression.function.IUserDefinedFunction;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionBool;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static io.viewserver.core.Utils.fromArray;

public class IsOrderVisible implements IUserDefinedFunction, IExpressionBool {
    private static final Logger log = LoggerFactory.getLogger(GetPartnerResponseField.class);
    private IExpressionString userIdField;
    private IExpressionString relationshipField;
    private IExpressionString orderDetailsField;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 3) {
            throw new IllegalArgumentException("Syntax: isOrderVisible(<userId (string)>,<relationships (json-string)>,<orderDetails (json-string)> ");
        }
        userIdField = (IExpressionString) parameters[0];
        relationshipField = (IExpressionString) parameters[1];
        orderDetailsField = (IExpressionString) parameters[2];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Bool;
    }

    @Override
    public boolean getBool(int row) {
        String userIdFieldString = userIdField.getString(row);
        if(userIdFieldString == null || "".equals(userIdFieldString)){
            log.warn("no userid so can't determine visibility of order");
            return true;
        }
        String userRelationshipFieldString = relationshipField.getString(row);
        UserRelationship[] relationships = JacksonSerialiser.getInstance().deserialise(userRelationshipFieldString,UserRelationship[].class);
        Optional<UserRelationship> relationship = fromArray(relationships).filter(c -> c.getToUserId().equals(userIdFieldString)).findAny();
        String orderDetailsString = orderDetailsField.getString(row);
        BasicOrder order = JacksonSerialiser.getInstance().deserialise(orderDetailsString,BasicOrder.class);
        if(relationship.isPresent()){
            UserRelationshipStatus relationshipStatus = relationship.get().getRelationshipStatus();
            if(relationshipStatus.equals(UserRelationshipStatus.BLOCKED) || relationshipStatus.equals(UserRelationshipStatus.BLOCKEDBYME)){
                return false;
            }
            if(order.isJustForFriends() != null && order.isJustForFriends()){
                return relationship.get().getRelationshipStatus().equals(UserRelationshipStatus.ACCEPTED);
            }
        }
        return order.isJustForFriends() == null || !order.isJustForFriends();
    }
}
