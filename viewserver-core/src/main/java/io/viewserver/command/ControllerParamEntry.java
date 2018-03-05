package io.viewserver.command;

public class ControllerParamEntry{
    String name;
    Class type;
    int index;
    private boolean required;

    public ControllerParamEntry(String name, Class type, int index, boolean required) {
        this.name = name;
        this.type = type;
        this.index = index;
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
