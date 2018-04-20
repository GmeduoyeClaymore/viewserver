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

package io.viewserver.report;

/**
 * Created by nickc on 31/10/2014.
 */

public class ParameterDefinition<TType> {
    private String name;
    private String label;
    private Class type;
    private boolean isRequired;
    private TType[] validValues;

    //TODO - add in optional valid values array
    public ParameterDefinition(){}

    public ParameterDefinition(String name, String label, Class<TType> type, boolean isRequired,TType... validValues) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.isRequired = isRequired;
        this.validValues = validValues;
    }

    public ParameterDefinition<TType> withRequired(boolean isRequired){
        this.isRequired = isRequired;
        return this;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label != null ? label : name;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public TType[] getValidValues() {
        return validValues;
    }

    public void setValidValues(TType[] validValues) {
        this.validValues = validValues;
    }
}
