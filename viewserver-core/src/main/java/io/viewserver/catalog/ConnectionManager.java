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

package io.viewserver.catalog;

import io.viewserver.Constants;
import io.viewserver.collections.IntHashSet;
import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.*;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnStringBase;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Created by nick on 10/03/2015.
 */
public class ConnectionManager extends InputOperatorBase implements IMetadataListener {
    public static final String OPERATOR_NAME = "connections";
    private static final String OUTPUT_OPERATOR_NAME_COLUMN = "outputOperator";
    private static final String OUTPUT_OPERATOR_TYPE_COLUMN = "outputOperatorType";
    private static final String OUTPUT_NAME_COLUMN = "output";
    private static final String INPUT_OPERATOR_NAME_COLUMN = "inputOperator";
    private static final String INPUT_OPERATOR_TYPE_COLUMN = "inputOperatorType";
    private static final String INPUT_NAME_COLUMN = "input";
    private Output output;
    private TIntObjectHashMap<Link> links = new TIntObjectHashMap<>(8, 0.75f, -1);
    private IntHashSet linkHashes = new IntHashSet(8, 0.75f, -1);

    public ConnectionManager(ExecutionContext executionContext, ICatalog catalog) {
        super(OPERATOR_NAME, executionContext, catalog);

        executionContext.getMetadataRegistry().addListener(this, true);

        output = new Output(Constants.OUT, this);
        addOutput(output);

        setSystemOperator(true);
    }

    @Override
    public void doTearDown() {
        getExecutionContext().getMetadataRegistry().removeListener(this);

        super.doTearDown();
    }

    @Override
    public void onRegisterLink(IOutput output, IInput input) {
        Link link = new Link(output, input);
        int hashCode = link.hashCode();
        links.put(hashCode, link);
        int rowId = linkHashes.addInt(hashCode);
        this.output.handleAdd(rowId);
    }

    @Override
    public void onUnregisterLink(IOutput output, IInput input) {
        Link link = new Link(output, input);
        int hashCode = link.hashCode();
        links.remove(hashCode);
        int rowId = linkHashes.index(hashCode);
        linkHashes.remove(hashCode);
        this.output.handleRemove(rowId);
    }

    @Override
    public void onRegisterInput(IInput input) {

    }

    @Override
    public void onUnregisterInput(IInput input) {

    }

    @Override
    public void onRegisterOutput(IOutput output) {

    }

    @Override
    public void onUnregisterOutput(IOutput output) {

    }

    private Link getLinkForRow(int row) {
        int hashCode = linkHashes.get(row);
        return links.get(hashCode);
    }

    private class Output extends OutputBase {
        public Output(String name, IOperator owner) {
            super(name, owner);

            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(new OutputOperatorNameColumn()));
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(new OutputOperatorTypeColumn()));
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(new OutputNameColumn()));
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(new InputOperatorNameColumn()));
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(new InputOperatorTypeColumn()));
            getSchema().addColumn(ColumnHolderUtils.createColumnHolder(new InputNameColumn()));
        }
    }

    private class OutputOperatorNameColumn extends ColumnStringBase {
        public OutputOperatorNameColumn() {
            super(OUTPUT_OPERATOR_NAME_COLUMN);
        }

        @Override
        public String getString(int row) {
            Link link = getLinkForRow(row);
            return link.getOutput().getOwner().getPath();
        }

        @Override
        public String getPreviousString(int row) {
            return null;
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }
    }

    private class OutputOperatorTypeColumn extends ColumnStringBase {
        public OutputOperatorTypeColumn() {
            super(OUTPUT_OPERATOR_TYPE_COLUMN);
        }

        @Override
        public String getString(int row) {
            Link link = getLinkForRow(row);
            return link.getOutput().getOwner().getClass().getName();
        }

        @Override
        public String getPreviousString(int row) {
            return null;
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }
    }

    private class OutputNameColumn extends ColumnStringBase {
        public OutputNameColumn() {
            super(OUTPUT_NAME_COLUMN);
        }

        @Override
        public String getString(int row) {
            Link link = getLinkForRow(row);
            return link.getOutput().getName();
        }

        @Override
        public String getPreviousString(int row) {
            return null;
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }
    }

    private class InputOperatorNameColumn extends ColumnStringBase {
        public InputOperatorNameColumn() {
            super(INPUT_OPERATOR_NAME_COLUMN);
        }

        @Override
        public String getString(int row) {
            Link link = getLinkForRow(row);
            return link.getInput().getOwner().getPath();
        }

        @Override
        public String getPreviousString(int row) {
            return null;
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }
    }

    private class InputOperatorTypeColumn extends ColumnStringBase {
        public InputOperatorTypeColumn() {
            super(INPUT_OPERATOR_TYPE_COLUMN);
        }

        @Override
        public String getString(int row) {
            Link link = getLinkForRow(row);
            return link.getInput().getOwner().getClass().getName();
        }

        @Override
        public String getPreviousString(int row) {
            return null;
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }
    }

    private class InputNameColumn extends ColumnStringBase {
        public InputNameColumn() {
            super(INPUT_NAME_COLUMN);
        }

        @Override
        public String getString(int row) {
            Link link = getLinkForRow(row);
            return link.getInput().getName();
        }

        @Override
        public String getPreviousString(int row) {
            return null;
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }
    }
}
