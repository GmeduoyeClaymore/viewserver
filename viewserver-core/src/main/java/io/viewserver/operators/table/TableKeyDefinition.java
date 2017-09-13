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
import java.util.HashMap;
import java.util.List;

/**
 * Created by paulrg on 26/11/2014.
 */
public class TableKeyDefinition{

    private final List<String> keys;
    private final String SEPARATOR = "@@";

    public TableKeyDefinition(String key){
        this.keys = Arrays.asList(key.replace(" ", "").split(","));
    }

    public TableKeyDefinition(String... keys) {
        this.keys = Arrays.asList(keys);
    }

    public String getValue(TableKey tableKey) {
        return StringUtils.join(tableKey.getValues(), SEPARATOR);
    }

    public String getValue(HashMap<String, Object> values){
        List<Object> keyValues = new ArrayList<>();

        int count = this.keys.size();
        for (int i = 0; i < count; i++) {
            keyValues.add(values.get(keys.get(i)));
        }

        return StringUtils.join(keyValues, SEPARATOR);
    }

    public String getKeyName(){
        return StringUtils.join(this.keys, SEPARATOR);
    }

    public int size() {
        return this.keys.size();
    }

    public List<String> getKeys() {
        return this.keys;
    }

    public String get(int index){
        return this.keys.get(index);
    }
}
