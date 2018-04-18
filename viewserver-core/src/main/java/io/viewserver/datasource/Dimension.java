/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.datasource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by nickc on 13/10/2014.
 */
@JsonIgnoreProperties({ "type" })
public class Dimension {
    private String name;
    private String sourceColumnName;
    private String label;
    private String group;
    private String plural;
    private Cardinality cardinality;
    private ColumnType columnType;
    private boolean global;

    public Dimension() {
    }

    public Dimension(String name, Cardinality cardinality, ColumnType columnType, boolean global) {
        this(name,name,cardinality,columnType, global);
    }
    public Dimension(String name, String sourceColumnName, Cardinality cardinality, ColumnType columnType, boolean global) {
        this.name = name;
        this.sourceColumnName = sourceColumnName;
        this.cardinality = cardinality;
        this.columnType = columnType;
        this.global = global;
    }

    public Dimension(String name, Cardinality cardinality, ColumnType columnType) {
        this(name,name,cardinality,columnType);
    }
    public Dimension(String name, String sourceColumnName, Cardinality cardinality, ColumnType columnType) {
        this.name = name;
        this.sourceColumnName = sourceColumnName;
        this.cardinality = cardinality;
        this.columnType = columnType;
    }


    public String getName() {
        return name;
    }

    public Dimension setName(String name) {
        this.name = name;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public Dimension setGroup(String group) {
        this.group = group;
        return this;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public void setColumnType(ColumnType columnType) {
        this.columnType = columnType;
    }

    public String getLabel() {
        return this.label != null ? this.label : this.name;
    }

    public Dimension setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getSourceColumnName() {
        return sourceColumnName;
    }

    public void setSourceColumnName(String sourceColumnName) {
        this.sourceColumnName = sourceColumnName;
    }

    public String getPlural() {
        return plural;
    }

    public Dimension setPlural(String plural) {
        this.plural = plural;
        return this;
    }

}
