package io.viewserver.util.dynamic;
import java.util.Map;
import java.util.Objects;

import static io.viewserver.util.dynamic.MethodInterpreters.binding;
import static io.viewserver.util.dynamic.MethodInterpreters.handlingDefaultMethods;
import static io.viewserver.util.dynamic.MethodInterpreters.handlingJsonMethods;

public class PropertyValueStore implements EqualisableByState {

    private final Class<?> iface;
    private final Map<String, Object> propertyValues;

    public PropertyValueStore(Class<?> iface, Map<String, Object> propertyValues) {
        this.iface = iface;
        this.propertyValues = propertyValues;
    }

    @Override
    public int hashCode() {
        return propertyValues.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s %s", iface, propertyValues);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PropertyValueStore)) {
            return false;
        }

        PropertyValueStore other = (PropertyValueStore) o;
        return Objects.equals(iface, other.iface)
                && Objects.equals(propertyValues, other.propertyValues);
    }

    public MethodInterpreter createMethodInterpreter() {
        return binding(this,
                handlingDefaultMethods(
                        PropertyMappingClassInterpreter.interpret(iface).bind(propertyValues)));
    }

    @Override
    public Object getState() {
        return this;
    }
}
