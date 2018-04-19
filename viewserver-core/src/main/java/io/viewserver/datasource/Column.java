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

import java.util.Arrays;
import java.util.EnumSet;


public class Column {
    private String name;
    private ContentType type;
    private EnumSet<ColumnOption> options = EnumSet.noneOf(ColumnOption.class);
    private Object defaultValue;

    public Column() {
    }

    public Column(String name, ContentType type) {
        this.name = name;
        this.type = type;
    }

    public Column withOptions(ColumnOption... options) {
        if (options.length > 0) {
            this.options.addAll(Arrays.asList(options));
        }
        return this;
    }

    public Column withDefaultValue(Object defaultValue){
        this.defaultValue = defaultValue;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue != null ? defaultValue : this.type.getColumnType().getDefaultValue();
    }

    public boolean hasOption(ColumnOption option) {
        return options.contains(option);
    }

    public EnumSet<ColumnOption> getOptions() {
        return options;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContentType getType() {
        return type;
    }

    public void setType(ContentType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Column{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", options=" + options +
                ", defaultValue=" + defaultValue +
                '}';
    }
}

