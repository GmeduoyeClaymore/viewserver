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

import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by nickc on 26/09/2014.
 */
public abstract class OperatorBase implements IOperator {
    private static final Logger log = LoggerFactory.getLogger(OperatorBase.class);
    private final String name;
    private final IExecutionContext executionContext;
    private ICatalog catalog;

    private final Map<String, IOutput> outputsByName = new HashMap<>();
    private final List<IOutput> outputs = new ArrayList<>();
    private final Map<String, IInput> inputsByName = new LinkedHashMap<>();
    private final List<IInput> inputs = new ArrayList<>();
    protected boolean isSchemaResetRequested = true;
    protected boolean isDataResetRequested = true;

    private int lastExecutionCount = -1;
    private int commitDependencyCount = 0;
    private volatile int inputsReadyCount = 0;

    private boolean isTornDown;
    private boolean initialised;
    private boolean isDataRefreshRequested;
    private boolean isSystemOperator;
    private EnumSet<Error> errors = EnumSet.noneOf(Error.class);
    private EnumSet<Status> statuses = EnumSet.noneOf(Status.class);
    private Map<String, Object> metadata;
    private String path;
    private Throwable lastConfigError;
    private Throwable lastSchemaError;
    private Throwable lastDataError;

    protected OperatorBase(String name, IExecutionContext executionContext, ICatalog catalog) {
        if (name.contains("/") && catalog != null) {
            throw new IllegalArgumentException("Operator names may not contain slashes.");
        }
        this.name = name;
        this.executionContext = executionContext;
        this.catalog = catalog;

        setPath();

        executionContext.register(this);
        if (catalog != null) {
            catalog.registerOperator(this);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    public boolean isDataResetRequested() {
        return isDataResetRequested;
    }

    private void setPath() {
        if (catalog == null) {
            path = name;
            return;
        }

        Stack<ICatalog> catalogs = new Stack<>();
        ICatalog thisCatalog = catalog;
        while (thisCatalog != null) {
            catalogs.push(thisCatalog);
            thisCatalog = thisCatalog.getParent();
        }

        StringBuilder builder = new StringBuilder();
        while (!catalogs.isEmpty()) {
            thisCatalog = catalogs.pop();
            builder.append(thisCatalog.getName());
            if (thisCatalog.getParent() != null) {
                builder.append('/');
            }
        }
        builder.append(name);
        path = builder.toString();
    }

    @Override
    public IExecutionContext getExecutionContext() {
        return executionContext;
    }

    @Override
    public IInput getInput(String name) {
        return inputsByName.get(name);
    }

    @Override
    public Map<String, IInput> getInputs() {
        return inputsByName;
    }

    protected void addInput(IInput input) {
        if (inputsByName.putIfAbsent(input.getName(), input) != null) {
            throw new RuntimeException(String.format("Input '%s' already exists", input.getName()));
        }
        inputs.add(input);
    }

    protected void removeInput(IInput input) {
        if (inputsByName.remove(input.getName()) == null) {
            throw new RuntimeException(String.format("Input '%s' does not exist", input.getName()));
        }
        inputs.remove(input);
    }

    @Override
    public IOutput getOutput(String name) {
        return outputsByName.get(name);
    }

    @Override
    public List<IOutput> getOutputs() {
        return outputs;
    }

    protected void addOutput(IOutput output) {
        if (outputsByName.containsKey(output.getName())) {
            throw new RuntimeException("Output " + output.getName() + " already exists");
        }

        outputsByName.put(output.getName(), output);
        outputs.add(output);
    }

    @Override
    public void inputReady(IInput input) {
        if (executionContext.getExecutionCount() != lastExecutionCount) {
            inputsReadyCount = 0;
            commitDependencyCount = inputsByName.size();
            if (this instanceof IInputOperator) {
                commitDependencyCount++;
            }

            lastExecutionCount = getExecutionContext().getExecutionCount();
        }
        inputsReadyCount++;
        if (inputsReadyCount == commitDependencyCount) {
            executionContext.submit(() -> commit(), 0);
        }
    }

    protected void commit() {
        if (!initialised && areInputSchemasReady()) {
            onInitialise();
            initialised = true;
        }

        if (initialised) {
            processStatuses();

            if (!hasStatus(Status.ConfigError)) {
                processConfig();

                if (!hasError(Error.ConfigError) && !hasStatus(Status.SchemaError)) {
                    onBeforeSchema();
                    processSchema();
                    onAfterSchema();

                    if (!hasError(Error.ConfigError) &&
                            !hasError(Error.SchemaError) && !hasStatus(Status.DataError)) {
                        processData();
                    } else if (hasStatus(Status.DataError)) {
                        propagateStatus(Status.DataError);
                    }
                } else if (hasStatus(Status.SchemaError)) {
                    propagateStatus(Status.SchemaError);
                }
            } else {
                propagateStatus(Status.ConfigError);
            }
        }

        commitOutputs();
    }

    private void processConfig() {
        boolean hadConfigError = hasError(Error.ConfigError);

        try {
            onProcessConfig();
        } catch (Throwable e) {
            if (e instanceof OperatorConfigurationException) {
                ((ConfigurableOperatorBase)this).setConfigFailed(e.getMessage());
            }

            if (!hasError(Error.ConfigError)) {
                setError(Error.ConfigError);
            }
            if (lastConfigError == null || !e.getMessage().equals(lastConfigError.getMessage())) {
                log.error(String.format("Error processing config for %s", this), e);
                lastConfigError = e;
            }
        }

        if (hadConfigError && !hasError(Error.ConfigError)) {
            propagateStatus(Status.ConfigErrorCleared);
        } else if (!hasError(Error.ConfigError) && hasStatus(Status.ConfigErrorCleared)) {
            propagateStatus(Status.ConfigErrorCleared);
        } else if (hasError(Error.ConfigError) || hasStatus(Status.ConfigError)) {
            propagateStatus(Status.ConfigError);
        }
    }

    protected void onProcessConfig() {
    }

    protected void onInitialise() {
        int size = inputs.size();
        for (int i = 0; i < size; i++) {
            inputs.get(i).onInitialise();
        }
        size = outputs.size();
        for (int i = 0; i < size; i++) {
            outputs.get(i).onInitialise();
        }
    }

    private boolean areInputSchemasReady() {
        int inputCount = inputs.size();
        for (int i = 0; i < inputCount; i++) {
            IInput input = inputs.get(i);
            if (input.getProducer() == null || input.getProducer().getSchema() == null) {
                return false;
            }
        }
        return true;
    }

    private void processStatuses() {
        statuses.clear();

        int inputCount = inputs.size();
        for (int i = 0; i < inputCount; i++) {
            processStatuses(inputs.get(i));
        }
    }

    private void processStatuses(IInput input) {
        List<Status> statuses = input.getProducer().getCurrentChanges().getStatuses();
        int count = statuses.size();
        for (int i = 0; i < count; i++) {
            Status status = statuses.get(i);
            switch (status) {
                case DataReset: {
                    input.resetData();
                    break;
                }
                case SchemaReset: {
                    input.resetSchema();
                    break;
                }
                default: {
                    setStatus(status);
                    break;
                }
            }
        }
    }

    protected void onBeforeSchema() {
    }

    private void processSchema() {
        try {
            if (hasError(Error.SchemaError)) {
                resetSchema();
            }

            if (!isSchemaResetRequested) {
                int count = inputs.size();
                for (int i = 0; i < count; i++) {
                    IInput input = inputs.get(i);
                    if (input.isSchemaResetRequested()) {
                        input.onSchemaResetRequested();
                    }
                }
            }

            if (isSchemaResetRequested) {
                isSchemaResetRequested = false;
                log.debug("Resetting schema on {}", this);
                onSchemaReset();
            } else {
                onSchemaChange();
            }

            if (hasError(Error.SchemaError)) {
                clearError(Error.SchemaError);
                lastSchemaError = null;
                log.info("Schema error cleared in {}", this);
                propagateStatus(Status.SchemaErrorCleared);
            } else if (hasStatus(Status.SchemaErrorCleared)) {
                propagateStatus(Status.SchemaErrorCleared);
            }
        } catch (Throwable e) {
            if (e instanceof OperatorConfigurationException) {
                ((ConfigurableOperatorBase)this).setConfigFailed(e.getMessage());
                if (!hasError(Error.ConfigError)) {
                    setError(Error.ConfigError);
                }
                if (lastConfigError == null || !Objects.equals(e.getMessage(), lastConfigError.getMessage())) {
                    log.error(String.format("Error processing config for %s",this), e);
                    lastConfigError = e;
                }
                propagateStatus(Status.ConfigError);
            } else {
                if (!hasError(Error.SchemaError)) {
                    setError(Error.SchemaError);
                }
                if (lastSchemaError == null || !Objects.equals(e.getMessage(), lastSchemaError.getMessage())) {
                    log.error(String.format("Error processing schema for %s", this), e);
                    lastSchemaError = e;
                }
                propagateStatus(Status.SchemaError);
            }
        }
    }

    private void propagateStatus(Status status) {
        int count = outputs.size();
        for (int i = 0; i < count; i++) {
            outputs.get(i).getCurrentChanges().handleStatus(status);
        }
    }

    private void setStatus(Status status) {
        statuses.add(status);
    }

    private void clearStatus(Status status) {
        statuses.remove(status);
    }

    private boolean hasStatus(Status status) {
        return statuses.contains(status);
    }

    private void setError(Error error) {
        errors.add(error);
    }

    private void clearError(Error error) {
        errors.remove(error);
    }

    private boolean hasError(Error error) {
        return errors.contains(error);
    }

    protected void onAfterSchema() {
    }

    protected void onSchemaReset() {
        onSchemaClear();
        int count = inputs.size();
        for (int i = 0; i < count; i++) {
            IInput input = inputs.get(i);
            input.resetSchema();
            input.onSchema();
        }
    }

    protected void onSchemaClear() {
        int count = outputs.size();
        for (int i = 0; i < count; i++) {
            IOutput output = outputs.get(i);
            output.clearSchema();
            output.clearData();
            output.resetSchema();
        }
    }

    private void onSchemaChange() {
        int count = inputs.size();
        for (int i = 0; i < count; i++) {
            inputs.get(i).onSchema();
        }
    }

    private void processData() {
        long start = System.nanoTime();
        try {
            if (hasError(Error.DataError)) {
                resetData();
            }

            if (!isDataResetRequested) {
                int count = inputs.size();
                for (int i = 0; i < count; i++) {
                    IInput input = inputs.get(i);
                    if (input.isDataResetRequested()) {
                        input.onDataResetRequested();
                    } else if (input.isDataRefreshRequested()) {
                        input.onDataRefreshRequested();
                    }
                }
            }

            if (isDataResetRequested) {
                isDataResetRequested = false;
                isDataRefreshRequested = false;
                log.debug("Resetting data on {}", this);
                onDataReset();
            } else if (isDataRefreshRequested) {
                isDataRefreshRequested = false;
                onDataRefresh();
            } else {
                onDataChange();
            }

            if (hasError(Error.DataError)) {
                clearError(Error.DataError);
                lastDataError = null;
                log.info("Data error cleared after reset for {}", this);
                propagateStatus(Status.DataErrorCleared);
            } else if (hasStatus(Status.DataErrorCleared)) {
                propagateStatus(Status.DataErrorCleared);
            }
        } catch (Throwable e) {
            if (!hasError(Error.DataError)) {
                setError(Error.DataError);
            }
            if (lastDataError == null || !Objects.equals(e.getMessage(), lastDataError.getMessage())) {
                log.error(String.format("Error processing data for %s", this), e);
                lastDataError = e;
            }
            propagateStatus(Status.DataError);
        }
        if (log.isTraceEnabled()) {
            log.trace(String.format("%s spent %3fms in processData()", getName(), (System.nanoTime() - start) / 1000000f));
        }
    }

    protected void onDataReset() {
        onDataClear();
        int count = inputs.size();
        for (int i = 0; i < count; i++) {
            IInput input = inputs.get(i);
            input.resetData();
            input.onData();
        }
    }

    private void onDataRefresh() {
        int count = inputs.size();
        for (int i = 0; i < count; i++) {
            inputs.get(i).onData();
        }
    }

    protected void onDataClear() {
        int count = inputs.size();
        for (int i = 0; i < count; i++) {
            inputs.get(i).onDataClear();
        }
        clearData();
    }

    private void clearData() {
        int count = outputs.size();
        for (int i = 0; i < count; i++) {
            IOutput output = outputs.get(i);
            output.resetData();
            output.clearData();
        }
    }

    private void onDataChange() {
        int count = inputs.size();
        for (int i = 0; i < count; i++) {
            inputs.get(i).onData();
        }
    }

    private void commitOutputs() {
        int count = outputs.size();
        for (int i = 0; i < count; i++) {
            outputs.get(i).commit();
        }
    }

    @Override
    public void onAfterCommit() {
        int count = inputs.size();
        for (int i = 0; i < count; i++) {
            inputs.get(i).onAfterCommit();
        }

        count = outputs.size();
        for (int i = 0; i < count; i++) {
            outputs.get(i).onAfterCommit();
        }
    }

    @Override
    public void resetSchema() {
        isSchemaResetRequested = true;
    }

    @Override
    public void resetData() {
        isDataResetRequested = true;
    }

    @Override
    public void refreshData() {
        isDataRefreshRequested = true;
    }


    @Override
    public void tearDown() {
        if (!isTornDown) {
            isTornDown = true;
            executionContext.tearDownOperator(this);
        }
    }

    @Override
    public void doTearDown() {
        int count = inputs.size();
            for (int i = 0; i < count; i++) {
            inputs.get(i).tearDown();
        }

        count = outputs.size();
        for (int i = 0; i < count; i++) {
            outputs.get(i).tearDown();
        }

        executionContext.unregister(this);
        catalog.unregisterOperator(this);
    }

    @Override
    public ICatalog getCatalog() {
        return catalog;
    }

    @Override
    public boolean isSystemOperator() {
        return isSystemOperator;
    }

    @Override
    public void setSystemOperator(boolean isSystemOperator) {
        this.isSystemOperator = isSystemOperator;
    }

    @Override
    public void setMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    @Override
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", path, getClass().getName());
    }

    private enum Error {
        ConfigError,
        SchemaError,
        DataError;
    }
}
