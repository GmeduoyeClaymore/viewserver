package io.viewserver.operators.validator;

public class ItemKey {
    private final String columnName;
    private String propertyName;

    public ItemKey(String keyString) {
        String[] parts = keyString.split("\\.");
        columnName = parts[0];
        if(parts.length > 1) {
            propertyName = parts[1];
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
