package io.viewserver.util.dynamic;

import java.util.Map;

public interface DynamicJsonBackedObject {
    DynamicJsonBackedObject set(String propertyName, Object propertyValue);

    String serialize(String... excludedFields);

    public Map<String,Object> getFields(String... excludedFields);

    Object get(String responseField);
}
