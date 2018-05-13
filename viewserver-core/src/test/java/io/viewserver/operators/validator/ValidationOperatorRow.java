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

package io.viewserver.operators.validator;

import java.util.HashMap;

public class ValidationOperatorRow {
    private HashMap<String,Object> values;
    private HashMap<String, Object> previousValue;
    private ValidationAction action;

    public ValidationOperatorRow(HashMap<String, Object> values,HashMap<String, Object> previousValue, ValidationAction action) {
        this.values = values;
        this.previousValue = previousValue;
        this.action = action;
    }

    public ValidationOperatorRow clone(ValidationAction newAction){
        return new ValidationOperatorRow(new HashMap<>(values),new HashMap<>(previousValue),newAction);
    }

    public int getRowId(String rowKeyField) {
        StringBuilder sb = new StringBuilder();
        String[] parts = rowKeyField.split(",");
        for(String part : parts){
            if(sb.length() > 0){
                sb.append(",");
            }
            String idString = values.get(part) + "";
            if("".equals(idString) || idString == null){
                throw new RuntimeException("Row does not contain a field named " + part);
            }
            sb.append(idString);
        }
        return ValidationUtils.rowKeyHash(sb + "_" + action.name());
    }

    public HashMap<String, Object> getValues() {
        return values;
    }

    public HashMap<String, Object> getPreviousValue() {
        return previousValue;
    }

    public ValidationAction getValidationAction() {
        return action;
    }
}

