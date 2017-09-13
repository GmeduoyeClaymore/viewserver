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

/**
 * Created by nick on 10/11/15.
 */
public class PartitionConfig {
    private String sourceTableName;
    private String partitionColumnName;
    private Object partitionValue;

    public PartitionConfig() {
    }

    public PartitionConfig(String sourceTableName, String partitionColumnName, Object partitionValue) {
        this.sourceTableName = sourceTableName;
        this.partitionColumnName = partitionColumnName;
        this.partitionValue = partitionValue;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    public String getPartitionColumnName() {
        return partitionColumnName;
    }

    public void setPartitionColumnName(String partitionColumnName) {
        this.partitionColumnName = partitionColumnName;
    }

    public Object getPartitionValue() {
        return partitionValue;
    }

    public void setPartitionValue(Object partitionValue) {
        this.partitionValue = partitionValue;
    }
}
