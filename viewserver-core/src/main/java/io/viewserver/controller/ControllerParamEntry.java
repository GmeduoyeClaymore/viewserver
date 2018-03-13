package io.viewserver.controller;

import io.viewserver.command.ActionParam;

import java.lang.annotation.Annotation;

public class ControllerParamEntry{
    ActionParam an;
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


    public ControllerParamEntry(ActionParam an,Class type, int index) {
        this(an.name(),type,index, an.required());
        this.an = an;
    }

    public boolean isRequired() {
        return required;
    }

    public ActionParam getAn() {
        return an;
    }

    public void setAn(ActionParam an) {
        this.an = an;
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
