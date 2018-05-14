package com.shotgun.viewserver.user;

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

public class IsBlocked implements IUserDefinedFunction, IExpressionBool {
    private static final Logger log = LoggerFactory.getLogger(GetPartnerResponseField.class);
    private IExpressionString userIdField;
    private IExpressionString relationshipField;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 2) {
            throw new IllegalArgumentException("Syntax: isBlocked(<userId (string)>,<relationships (json-string)>");
        }
        userIdField = (IExpressionString) parameters[0];
        relationshipField = (IExpressionString) parameters[1];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Bool;
    }

    @Override
    public boolean getBool(int row) {
        String userIdFieldString = userIdField.getString(row);
        if(userIdFieldString == null || "".equals(userIdFieldString)){
            return false;
        }
        String userRelationshipFieldString = relationshipField.getString(row);
        UserRelationship[] relationships = JacksonSerialiser.getInstance().deserialise(userRelationshipFieldString,UserRelationship[].class);
        Optional<UserRelationship> relationship = fromArray(relationships).filter(c -> c.getToUserId().equals(userIdFieldString)).findAny();
        if(relationship.isPresent()){
            UserRelationshipStatus relationshipStatus = relationship.get().getRelationshipStatus();
            return relationshipStatus.equals(UserRelationshipStatus.BLOCKED) || relationshipStatus.equals(UserRelationshipStatus.BLOCKEDBYME);
        }
        return false;
    }
}
