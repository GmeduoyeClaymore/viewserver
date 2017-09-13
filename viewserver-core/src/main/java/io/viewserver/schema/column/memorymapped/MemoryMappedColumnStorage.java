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

package io.viewserver.schema.column.memorymapped;

import io.viewserver.schema.column.ColumnStorageBase;

import java.nio.file.Path;

/**
 * Created by nick on 27/10/15.
 */
public class MemoryMappedColumnStorage extends ColumnStorageBase {
    private Path schemaDirectory;

    public MemoryMappedColumnStorage(Path schemaDirectory) {
        super(new MemoryMappedColumnFactory());
        this.schemaDirectory = schemaDirectory;
    }

    public Path getSchemaDirectory() {
        return schemaDirectory;
    }
}
