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

package io.viewserver.operators;

import io.viewserver.messages.tableevent.IStatus;

/**
 * Created by bemm on 20/10/2014.
 */
public enum Status {
    DataReset(1),
    SchemaReset(2),
    SchemaError(3),
    SchemaErrorCleared(4),
    ConfigError(5),
    ConfigErrorCleared(6),
    DataError(7),
    DataErrorCleared(8);

    private int id;

    Status(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Status getStatus(int id) {
        switch (id) {
            case 1: {
                return DataReset;
            }
            case 2: {
                return SchemaReset;
            }
            default: {
                throw new IllegalArgumentException("Invalid status id");
            }
        }
    }

    public IStatus.StatusId serialise() {
        switch (this) {
            case DataReset: {
                return IStatus.StatusId.DataReset;
            }
            case SchemaReset: {
                return IStatus.StatusId.SchemaReset;
            }
            case SchemaError: {
                return IStatus.StatusId.SchemaError;
            }
            case SchemaErrorCleared: {
                return IStatus.StatusId.SchemaErrorCleared;
            }
            case ConfigError: {
                return IStatus.StatusId.ConfigError;
            }
            case ConfigErrorCleared: {
                return IStatus.StatusId.ConfigErrorCleared;
            }
            case DataError: {
                return IStatus.StatusId.DataError;
            }
            case DataErrorCleared: {
                return IStatus.StatusId.DataErrorCleared;
            }
            default: {
                throw new UnsupportedOperationException("Cannot serialise status '" + this + "'");
            }
        }
    }

    public static Status fromDto(IStatus.StatusId statusId) {
        switch (statusId) {
            case DataReset: {
                return DataReset;
            }
            case SchemaReset: {
                return SchemaReset;
            }
            case SchemaError: {
                return SchemaError;
            }
            case SchemaErrorCleared: {
                return SchemaErrorCleared;
            }
            case DataError: {
                return DataError;
            }
            case DataErrorCleared: {
                return DataErrorCleared;
            }
            case ConfigError: {
                return ConfigError;
            }
            case ConfigErrorCleared: {
                return ConfigErrorCleared;
            }
            default: {
                throw new IllegalArgumentException("Cannot deserialise status '" + statusId + "'");
            }
        }
    }
}
