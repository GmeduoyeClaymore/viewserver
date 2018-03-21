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

package io.viewserver.server;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.firebase.FirebaseDataAdapter;
import io.viewserver.adapters.firebase.FirebaseUtils;
import io.viewserver.datasource.IDataLoader;
import io.viewserver.datasource.ILocalStorageDataAdapterFactory;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.schema.column.IRowFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by nick on 17/02/2015.
 */
public class FirebaseInstallingDataAdapterFactory implements ILocalStorageDataAdapterFactory {
    private static final Logger log = LoggerFactory.getLogger(FirebaseInstallingDataAdapterFactory.class);
    private final String firebaseKeyPath;

    public FirebaseInstallingDataAdapterFactory(String firebaseKeyPath) {
        log.info("Using firebase database with key {}", firebaseKeyPath);
        this.firebaseKeyPath = firebaseKeyPath;
    }

    @Override
    public IDataLoader getAdapter(String name, String tableName, int batchSize) {
        return new DataLoader(
                name,
                new FirebaseDataAdapter(firebaseKeyPath, tableName),
                null
        ).withBatchSize(batchSize);
    }
}
