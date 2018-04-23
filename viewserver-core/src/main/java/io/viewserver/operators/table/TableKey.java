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

package io.viewserver.operators.table;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableKey {
    private ArrayList<Object> keyValues;

    public TableKey(Object... keyValues) {
        this.keyValues = new ArrayList<Object>(Arrays.asList(keyValues));
    }

    public void addKeyValue(Object keyValue){
        this.keyValues.add(keyValue);
    }

    public int size(){
        return this.keyValues.size();
    }

    public List<Object> getValues() {
        return keyValues;
    }

    public void join(TableKey otherKey){
        this.keyValues.addAll(otherKey.getValues());
    }

    @Override
    public String toString(){
        return toString(",");
    }

    public String toString(String delimiter) {

        List<String> result  = new ArrayList<String>();
        for(Object obj : keyValues){
            result.add(printParam(obj));
        }
        return StringUtils.join(keyValues, delimiter);
    }

    private String printParam(Object obj) {
        if(obj == null){
            return "NULL:NULL";
        }
        return String.format("[\"%s\":%s]",obj.toString(),obj.getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableKey tableKey = (TableKey) o;

        if (keyValues != null ? !keyValues.equals(tableKey.keyValues) : tableKey.keyValues != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return keyValues != null ? keyValues.hashCode() : 0;
    }
}
