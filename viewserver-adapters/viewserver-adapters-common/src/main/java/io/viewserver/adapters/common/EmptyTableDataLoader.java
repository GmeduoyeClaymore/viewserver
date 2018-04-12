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

package io.viewserver.adapters.common;

import io.viewserver.datasource.IRecord;

import java.util.function.Consumer;

/**
 * Created by nick on 15/07/15.
 */
public class EmptyTableDataLoader extends DataLoader {
    public EmptyTableDataLoader() {
        this(null);
    }

    public EmptyTableDataLoader(String name) {
        super(name, null, null);
    }

    @Override
    protected void loadSnapshot(Consumer<IRecord> consumer) {
        loadDataFuture.set(true);
    }

    public void setName(String name) {
        this.name = name;
    }
}
