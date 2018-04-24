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

package io.viewserver.configurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bemm on 31/10/2014.
 */
public interface IConfiguratorSpec {
    List<OperatorSpec> getOperators();

    void reset();

    public static class OperatorSpec {
        private List<Connection> connectionsList = new ArrayList<>();
        private String name;
        private String type;
        private Operation operation;
        private Object config;
        private boolean distributed;
        private Map<String, Object> metadata = new HashMap<>();

        public OperatorSpec(){}

        public OperatorSpec(String name, String type, Operation operation, boolean distributed, Object config) {
            this.name = name;
            this.type = type;
            this.operation = operation;
            this.distributed = distributed;
            this.config = config;
        }

        public String getName() {
            return name;
        }

        public Operation getOperation() {
            return operation;
        }

        public String getType() {
            return type;
        }

        public List<Connection> getConnections() {
            return connectionsList;
        }

        public Object getConfig() {
            return config;
        }

        public boolean isDistributed() {
            return distributed;
        }

        public void addMetadata(Map<String, Object> metadata) {
            this.metadata.putAll(metadata);
        }

        public void addMetadata(String key, Object value) {
            this.metadata.put(key, value);
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public enum Operation {
            CreateConfigure,
            Remove
        }

        public void setConnectionsList(List<Connection> connectionsList) {
            this.connectionsList = connectionsList;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setOperation(Operation operation) {
            this.operation = operation;
        }

        public void setConfig(Object config) {
            this.config = config;
        }

        @Override
        public String toString() {
            return "OperatorSpec{" +
                    "operation=" + operation +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", connectionsList=" + connectionsList +
                    ", config=" + config +
                    ", distributed=" + distributed +
                    '}';
        }
    }

    public static class Connection {
        private String operator;
        private String output;
        private String input;
        private boolean unplugExisting;

        public Connection(){}

        public Connection(String operator, String output, String input) {
            this.operator = operator;
            this.output = output;
            this.input = input;
        }

        public String getOperator() {
            return operator;
        }

        public String getOutput() {
            return output;
        }

        public String getInput() {
            return input;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public void setOutput(String output) {
            this.output = output;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public boolean getUnplugExisting() {
            return unplugExisting;
        }

        public void setUnplugExisting(boolean unplugExisting) {
            this.unplugExisting = unplugExisting;
        }

        @Override
        public String toString() {
            return "Connection{" +
                    "operator='" + operator + '\'' +
                    ", output='" + output + '\'' +
                    ", input='" + input + '\'' +
                    ", unplugExisting=" + unplugExisting +
                    '}';
        }
    }
}
