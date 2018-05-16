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

package io.viewserver.operators.projection;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.changequeue.ChangeQueue;
import io.viewserver.changequeue.IChangeQueue;
import io.viewserver.changequeue.IMappedChangeQueue;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.*;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.IRowFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bemm on 31/10/2014.
 */
public class ProjectionOperator extends ConfigurableOperatorBase<IProjectionConfig> {
    private static final Logger log = LoggerFactory.getLogger(ProjectionOperator.class);
    private Input input;
    private Output output;
    private IProjectionConfig.ProjectionMode mode;
    private Map<String, IProjectionConfig.ProjectionColumn> projectionColumns = new HashMap<>();
    private Map<IProjectionConfig.ProjectionColumn, Pattern> regexProjectionColumns = new HashMap<>();

    public ProjectionOperator(String name, IExecutionContext executionContext, ICatalog catalog) {
        super(name, executionContext, catalog);

        input = new Input(Constants.IN, this);
        addInput(input);

        output = new Output(Constants.OUT, this);
        addOutput(output);
        register();
    }

    @Override
    protected IProjectionConfig mergePendingConfig(IProjectionConfig pendingConfig, IProjectionConfig newConfig) {
        if (!pendingConfig.getMode().equals(newConfig.getMode())) {
            throw new IllegalStateException("Cannot merge conflicting projection modes");
        }
        return new IProjectionConfig() {
            @Override
            public ProjectionMode getMode() {
                return pendingConfig.getMode();
            }

            @Override
            public Collection<ProjectionColumn> getProjectionColumns() {
                ArrayList<ProjectionColumn> projectionColumns = new ArrayList<>();
                projectionColumns.addAll(pendingConfig.getProjectionColumns());
                for (ProjectionColumn projectionColumn : newConfig.getProjectionColumns()) {
                    if (!projectionColumns.contains(projectionColumn)) {
                        projectionColumns.add(projectionColumn);
                    }
                }
                return projectionColumns;
            }
        };
    }

    @Override
    protected void processConfig(IProjectionConfig config) {
        if (configChanged(config)) {
            mode = config.getMode();

            for (IProjectionConfig.ProjectionColumn projectionColumn : config.getProjectionColumns()) {
                if (projectionColumn.isRegex()) {
                    regexProjectionColumns.put(projectionColumn, Pattern.compile(projectionColumn.getInboundName()));
                } else {
                    projectionColumns.put(projectionColumn.getInboundName(), projectionColumn);
                }
            }

            input.resetSchema();
        }
    }

    private boolean configChanged(IProjectionConfig config) {
        if (this.config == null) {
            return true;
        }

        return !config.getMode().equals(this.config.getMode())
                || !config.getProjectionColumns().equals(this.config.getProjectionColumns());
    }

    public IInput getInput() {
        return input;
    }

    public IOutput getOutput() {
        return output;
    }

    // note that the onRowAdd, onRowUpdate and onRowRemove methods are deliberately omitted here - we do not need
    // to add them to the output, as we have some optimisations around row and change tracking in the classes below
    private class Input extends InputBase {
        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        protected void onRowAdd(int row) {
            output.handleAdd(row);
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            output.handleUpdate(row);
        }

        @Override
        protected void onRowRemove(int row) {
            output.handleRemove(row);
        }


        @Override
        public void onSchemaResetRequested() {
            super.onSchemaResetRequested();
        }

        @Override
        public void resetSchema() {
            super.resetSchema();
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            String inboundName = columnHolder.getName();
            IProjectionConfig.ProjectionColumn projectionColumn = projectionColumns.get(inboundName);
            boolean projected = false;
            if (projectionColumn != null) {
                if (mode.equals(IProjectionConfig.ProjectionMode.Exclusionary)) {
                    return;
                }
                List<String> outboundNames = projectionColumn.getOutboundNames();
                if (!outboundNames.isEmpty()) {
                    int count = outboundNames.size();
                    for (int i = 0; i < count; i++) {
                        addProjectedColumn(columnHolder, outboundNames.get(i));
                    }
                } else if (mode.equals(IProjectionConfig.ProjectionMode.Inclusionary)) {
                    addProjectedColumn(columnHolder, inboundName);
                } else {
                    return;
                }
                projected = true;
            } else {
                regexProjectionChecker.columnHolder = columnHolder;
                regexProjectionChecker.inboundName = inboundName;
                regexProjectionColumns.entrySet().forEach(regexProjectionChecker.checkRegexProc);
                projected = regexProjectionChecker.projected;
                regexProjectionChecker.clear();
            }
            if (!projected) {
                if (mode.equals(IProjectionConfig.ProjectionMode.Inclusionary)) {
                    return;
                }
                // Projection mode
                addProjectedColumn(columnHolder, inboundName);
            }
        }

        private void addProjectedColumn(ColumnHolder inboundHolder, String outboundName) {
            ColumnHolder outHolder = output.getColumnHolderFactory().createColumnHolder(outboundName, inboundHolder);
            output.mapColumn(inboundHolder, outHolder, getProducer().getCurrentChanges());
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
            output.unmapColumn(columnHolder);
        }

        private class RegexProjectionChecker {
            private ColumnHolder columnHolder;
            private String inboundName;
            private boolean projected;
            private final Consumer<Map.Entry<IProjectionConfig.ProjectionColumn, Pattern>> checkRegexProc = regexProjection -> {
                if (projected) {
                    return;
                }
                IProjectionConfig.ProjectionColumn projectionColumn = regexProjection.getKey();
                Pattern pattern = regexProjection.getValue();
                Matcher matcher = pattern.matcher(inboundName);
                if (matcher.find()) {
                    projected = true;
                    if (mode.equals(IProjectionConfig.ProjectionMode.Exclusionary)) {
                        return;
                    }
                    List<String> outboundNames = projectionColumn.getOutboundNames();
                    if (!outboundNames.isEmpty()) {
                        int count = outboundNames.size();
                        for (int i = 0; i < count; i++) {
                            String outboundName = outboundNames.get(i);
                            for (int j = 0; j <= matcher.groupCount(); j++) {
                                outboundName = outboundName.replace("$" + j, matcher.group(j));
                            }
                            addProjectedColumn(columnHolder, outboundName);
                        }
                    } else if (mode.equals(IProjectionConfig.ProjectionMode.Inclusionary)) {
                        addProjectedColumn(columnHolder, inboundName);
                    }
                }
            };

            public void clear() {
                columnHolder = null;
                inboundName = null;
                projected = false;
            }
        }
        private final RegexProjectionChecker regexProjectionChecker = new RegexProjectionChecker();
    }

    private class Output extends MappedOutputBase {
        private final ProjectionChangeQueue changeQueue;

        public Output(String name, IOperator owner) {
            super(name, owner);
            changeQueue = new ProjectionChangeQueue(this);
        }



        @Override
        public IColumnHolderFactory getColumnHolderFactory() {
            return new PassThroughColumnHolderFactory();
        }

        @Override
        public ActiveRowTracker getRowTracker() {
            // the projection operator is a straight-through operator - all rows in its producer will be active
            // therefore, just reuse the producer's row tracker
            return input.getProducer().getRowTracker();
        }

        @Override
        public IMappedChangeQueue getCurrentChanges() {
            return changeQueue;
        }

        @Override
        public void clearData() {
            // don't clear data here - it belongs upstream
        }
    }

    // simplified implementation of the change queue for the projection operator
    // since it is a straight-through operator, we only need to map column IDs,
    // and delegate everything else upstream
    private class ProjectionChangeQueue extends ChangeQueue implements IMappedChangeQueue {
        private int[] columnMapping;
        private boolean affectsReferenceCounts;

        public ProjectionChangeQueue(IOutput owner) {
            super(owner);
            columnMapping = new int[32];
        }

        @Override
        public boolean isDirty(int rowId, int columnId) {
            try {
                IChangeQueue upstreamChanges = input.getProducer().getCurrentChanges();
                if (upstreamChanges != null) {
                    int inboundColumnId = columnMapping[columnId];
                    if (inboundColumnId == -1) {
                        return false;
                    }

                    return upstreamChanges.isDirty(rowId, inboundColumnId);
                }
            } catch (Throwable ex) {
                return false;
            }

            return false;
        }

        @Override
        public void mapColumn(ColumnHolder inboundColumn, ColumnHolder outboundColumn, IChangeQueue sourceChangeQueue) {
            int outboundId = outboundColumn.getColumnId();
            if (outboundId == -1) {
                throw new IllegalStateException("Column '" + outboundColumn.getName() + "' must be added to the output schema before mapping");
            }
            ensureColumnMappingCapacity(outboundId + 1);
            columnMapping[outboundId] = inboundColumn.getColumnId();
        }

        @Override
        public int getOutboundColumnId(ColumnHolder inboundColumn) {
            return columnMapping[inboundColumn.getColumnId()];
        }

        @Override
        public void setAffectsReferenceCounts(boolean affectsReferenceCounts) {
            this.affectsReferenceCounts = affectsReferenceCounts;
        }

        @Override
        public void handleAdd(int row) {
            super.handleAdd(row);

            if (affectsReferenceCounts) {
                if (input.getProducer().getCurrentChanges().isReferenceCountingEnabled(true)) {
                    input.getProducer().getCurrentChanges().incrementReferenceCount(row);
                }
            }
        }

        @Override
        public void handleRemove(int row) {
            super.handleRemove(row);

            if (affectsReferenceCounts) {
                if (input.getProducer().getCurrentChanges().isReferenceCountingEnabled(true)) {
                    input.getProducer().getCurrentChanges().decrementReferenceCount(row);
                }
            }
        }

        @Override
        public boolean isReferenceCountingEnabled(boolean checkUpstream) {
            if (super.isReferenceCountingEnabled(checkUpstream)) {
                return true;
            }

            if (input.getProducer().getCurrentChanges().isReferenceCountingEnabled(true)) {
                return true;
            }

            return false;
        }

        @Override
        public void incrementReferenceCount(int rowId) {
            super.incrementReferenceCount(rowId);

            if (input.getProducer().getCurrentChanges().isReferenceCountingEnabled(true)) {
                input.getProducer().getCurrentChanges().incrementReferenceCount(rowId);
            }
        }

        @Override
        public void decrementReferenceCount(int rowId) {
            super.decrementReferenceCount(rowId);

            if (input.getProducer().getCurrentChanges().isReferenceCountingEnabled(true)) {
                input.getProducer().getCurrentChanges().decrementReferenceCount(rowId);
            }
        }

        private void ensureColumnMappingCapacity(int capacity) {
            int oldLength = columnMapping.length;
            if (oldLength < capacity) {
                columnMapping = Arrays.copyOf(columnMapping, capacity);
                for (int i = oldLength; i < capacity; i++) {
                    columnMapping[i] = -1;
                }
            }
        }

        @Override
        public boolean columnHasDirty(int columnId) {
            try {
                IChangeQueue upstreamChanges = input.getProducer().getCurrentChanges();
                if (upstreamChanges != null) {
                    int inboundColumnId = columnMapping[columnId];
                    if (inboundColumnId == -1) {
                        return false;
                    }
                    return upstreamChanges.columnHasDirty(inboundColumnId);
                }
            } catch (Throwable ex) {
                log.error("Problem checking column dirty flags", ex);
                return false;
            }

            return false;
        }

        @Override
        public boolean hasChanges() {
            return (input.getProducer() != null && input.getProducer().getCurrentChanges().hasChanges()) || super.hasChanges();
        }

        @Override
        public ChangeQueue.Cursor createCursor() {
            return new Cursor(input.getProducer().getCurrentChanges());
        }

        @Override
        public boolean getNext(ChangeQueue.Cursor cursor) {
            return input.getProducer().getCurrentChanges().getNext(cursor);
        }

        @Override
        public boolean hasUpdates() {
            return input.getProducer().getCurrentChanges().hasUpdates();
        }

        private class Cursor extends ChangeQueue.Cursor {
            private IChangeQueue changeQueue;

            private Cursor(IChangeQueue changeQueue) {
                super(changeQueue);
                this.changeQueue = changeQueue;
            }

            @Override
            public boolean isDirty(int columnId) {
                int inboundColumnId = columnMapping[columnId];
                if (inboundColumnId == -1) {
                    return false;
                }
                return changeQueue.isDirty(getRowId(), inboundColumnId);
            }
        }
    }
}
