package com.shotgun.viewserver.user;

import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.ContentType;
import io.viewserver.operators.spread.ISpreadFunction;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRelationshipsSpreadFunction implements ISpreadFunction {
    private static final Logger log = LoggerFactory.getLogger(UserRelationshipsSpreadFunction.class);
    private static final Column toUserId;
    private static final Column relationshipStatus;
    private static final Column relationshipType;


    public static String NAME = "userRelationshipsSpread";
    public static String TO_USER_ID_COLUMN = "toUserId";
    public static String REALTIONSHIP_STATUS_COLUMN = "relationshipStatus";
    public static String RELATIONSHIP_TYPE_COLUMN = "relationshipType";
    private static List<Column> columns = new ArrayList<>();


    static{
        toUserId = new Column(TO_USER_ID_COLUMN, ContentType.String);
        relationshipStatus = new Column(REALTIONSHIP_STATUS_COLUMN,ContentType.String);
        relationshipType = new Column(RELATIONSHIP_TYPE_COLUMN,ContentType.String);
        columns.add(toUserId);
        columns.add(relationshipStatus);
        columns.add(relationshipType);

    }
    public UserRelationshipsSpreadFunction() {
    }

    @Override
    public List<Column> getColumns(){
        return this.columns;
    }

    @Override
    public List<Map.Entry<Column, Object[]>> getValues(int row, ColumnHolder columnHolder) {
        String relationshipsJSONString = (String) ColumnHolderUtils.getValue(columnHolder, row);
        if(relationshipsJSONString ==null || "".equals(relationshipsJSONString)){
            return new ArrayList<>();
        }
        UserRelationship[] relationships = JacksonSerialiser.getInstance().deserialise(relationshipsJSONString, UserRelationship[].class);
        int length;
        if(relationships == null){
            length = 1;
        }else{
            length = relationships.length;
        }
        Object[] toUserId = new Object[length];
        Object[] relationshipStatus = new Object[length];
        Object[] relationshipTypes = new Object[length];

        if(relationships != null) {
            for (int i = 0; i < length; i++) {
                UserRelationship negotiationResponse = relationships[i];
                toUserId[i] = negotiationResponse.getToUserId();
                relationshipStatus[i] = negotiationResponse.getRelationshipStatus();
                relationshipTypes[i] = negotiationResponse.getUserRelationshipType();
            }
        }


        List<HashMap.Entry<Column,Object[]>> result = new ArrayList<>();
        result.add(new HashMap.SimpleEntry<>(UserRelationshipsSpreadFunction.toUserId, toUserId));
        result.add(new HashMap.SimpleEntry<>(UserRelationshipsSpreadFunction.relationshipStatus, relationshipStatus));
        result.add(new HashMap.SimpleEntry<>(UserRelationshipsSpreadFunction.relationshipType, relationshipTypes));
        return result;
    }

}
