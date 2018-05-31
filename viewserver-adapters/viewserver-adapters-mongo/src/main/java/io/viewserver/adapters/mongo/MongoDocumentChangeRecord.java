package io.viewserver.adapters.mongo;

import io.viewserver.adapters.common.Record;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.ContentType;
import io.viewserver.datasource.SchemaConfig;
import org.bson.*;
import org.bson.json.Converter;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bson.json.StrictJsonWriter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.lang.String.format;

/**
 * Created by Paul on 15/03/2018.
 */

public class MongoDocumentChangeRecord extends Record {
    private final SchemaConfig schema;
    private JsonWriterSettings settings = JsonWriterSettings.builder().dateTimeConverter(new OurShellDateTimeConverter()).build();
    ;

    public MongoDocumentChangeRecord(SchemaConfig schema, Document doc) {
        this.schema = schema;
        initialise(doc);
    }

    public MongoDocumentChangeRecord(SchemaConfig schema, BsonDocument doc) {
        this.schema = schema;
        initialise(doc);
    }

    public void setId(String id){
        values.put("_id",id);
        List<String> primaryKey = schema.getKeyColumns();
        if(primaryKey.size() != 1){
            throw new RuntimeException("Cannot set id as " + primaryKey.size() + "  columns are specified as keys in the schema");
        }
        values.put(primaryKey.get(0),id);
    }

    private void initialise(Document doc) {
        values.clear();

        setValuesFrom(doc);
    }
    private void initialise(BsonDocument doc) {
        values.clear();
        setValuesFrom(doc);
    }

    public void setValuesFrom(Document doc) {
        for (Column column : schema.getColumns()) {
            String columnName = column.getName();
            if (!doc.containsKey(columnName)) {
                continue;
            }
            values.put(columnName, getMongoDocumentValue(column.getType(), columnName, doc));
        }
    }

    public void setValuesFrom(BsonDocument doc) {
        for (Column column : schema.getColumns()) {
            String columnName = column.getName();
            if (!doc.containsKey(columnName)) {
                continue;
            }
            values.put(columnName, getMongoDocumentValue(column.getType(), columnName, doc));
        }
    }

    public Object getMongoDocumentValue(ContentType dataType, String colName, BsonDocument doc) {
        if(doc.isNull(colName)){
            return null;
        }
        switch (dataType) {
            case Bool: {
                BsonBoolean aBoolean = doc.getBoolean(colName);
                return aBoolean.getValue();
            }
            case Short: {
                BsonInt32 int32 = doc.getInt32(colName);
                return int32.getValue();
            }
            case Int: {
                BsonInt32 int32 = doc.getInt32(colName);
                return int32.getValue();
            }
            case Long: {
                BsonInt64 int64 = doc.getInt64(colName);
                return int64.getValue();
            }
            case Float: {
                BsonDouble aDouble = doc.getDouble(colName);
                return aDouble.getValue();
            }
            case Double: {
                BsonDouble aDouble = doc.getDouble(colName);
                return aDouble.getValue();
            }
            case String: {

                BsonString string = doc.getString(colName);
                return string.getValue();
            }
            case Json: {
                BsonValue bsonValue = doc.get(colName);
                return getStringFromVal(bsonValue);
            }
            case DateTime: {
                return doc.getDateTime(colName) != null ? doc.getDateTime(colName).getValue(): null;
            }
            case Date: {
                return doc.getDateTime(colName) != null ? doc.getDateTime(colName).getValue() : null;
            }
            default:
                throw new RuntimeException(String.format("Could not process column of type %s", dataType));
        }
    }

    private String getStringFromVal(BsonValue bsonValue) {
        if(bsonValue instanceof BsonString){
            return ((BsonString)bsonValue).getValue();
        }
        if(bsonValue instanceof BsonDocument){
            String s = ((BsonDocument) bsonValue).toJson(settings);
            return s;
        }
        if(bsonValue instanceof BsonArray){
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for(BsonValue val : ((BsonArray)bsonValue).getValues()){
                String result = getStringFromVal(val);
                if(result ==null){
                    continue;
                }
                if(sb.length()>1) {
                    sb.append(",");
                }
                sb.append(result);

            }
            sb.append("]");
            return sb.toString();
        }
        throw new RuntimeException("Unrecognised bson value" + bsonValue);
    }

    public Object getMongoDocumentValue(ContentType dataType, String colName, Document doc) {
        switch (dataType) {
            case Bool: {
                return doc.getBoolean(colName);
            }
            case Short: {
                return doc.getLong(colName).shortValue();
            }
            case Int: {
                return doc.getInteger(colName);
            }
            case Long: {
                return doc.getLong(colName);
            }
            case Float: {
                return doc.getDouble(colName).floatValue();
            }
            case Double: {
                return doc.getDouble(colName);
            }
            case String: {
                return doc.getString(colName);
            }
            case Json: {
                return doc.get(colName);
            }
            case DateTime: {
                return doc.getDate(colName) != null ? doc.getDate(colName).getTime() : null;
            }
            case Date: {
                return doc.getDate(colName) != null ? doc.getDate(colName).getTime() : null;
            }
            default:
                throw new RuntimeException(String.format("Could not process column of type %s", dataType));
        }
    }
}

class OurShellDateTimeConverter implements Converter<Long> {
    @Override
    public void convert(final Long value, final StrictJsonWriter writer) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (value >= -59014396800000L && value <= 253399536000000L) {
            writer.writeRaw(format("\"%s\"", dateFormat.format(new Date(value))));
        } else {
            writer.writeRaw(format("new Date(%d)", value));
        }
    }
}

