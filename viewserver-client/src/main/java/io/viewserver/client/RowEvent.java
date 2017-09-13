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

package io.viewserver.client;

import java.util.Map;

/**
 * Created by nick on 11/02/2015.
 */
public class RowEvent {
    private int rowId;
    private RowEventType type;
    private Map<Integer, Object> values;

    public RowEvent(int rowId, RowEventType type) {
        this.rowId = rowId;
        this.type = type;
    }

    public int getRowId() {
        return rowId;
    }

    public RowEventType getType() {
        return type;
    }

    public Map<Integer, Object> getValues() {
        return values;
    }

    public static RowEvent addRow(Map<Integer, Object> values) {
        RowEvent rowEvent = new RowEvent(-1, RowEventType.Add);
        rowEvent.values = values;
        return rowEvent;
    }

    public static RowEvent updateRow(int rowId, Map<Integer, Object> values) {
        RowEvent rowEvent = new RowEvent(rowId, RowEventType.Update);
        rowEvent.values = values;
        return rowEvent;
    }

    public static RowEvent removeRow(int rowId) {
        return new RowEvent(rowId, RowEventType.Remove);
    }
}
