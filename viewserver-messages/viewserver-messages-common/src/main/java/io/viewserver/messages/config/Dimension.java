package io.viewserver.messages.config;

import io.viewserver.messages.common.ColumnType;
import io.viewserver.messages.common.ContentType;

public class Dimension {
    private String namespace;
    private String name;
    private String label;
    private String group;
    private String plural;
    private Cardinality cardinality;
    private ContentType contentType;
    private boolean global;
    private boolean imported;

    public Dimension(String namespace, String name, String label, String group, String plural, Cardinality cardinality, ContentType contentType, boolean global) {
        this.namespace = namespace;
        this.name = name;
        this.label = label;
        this.group = group;
        this.plural = plural;
        this.cardinality = cardinality;
        this.contentType = contentType;
        this.global = global;
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPlural() {
        return plural;
    }

    public void setPlural(String plural) {
        this.plural = plural;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }
}

