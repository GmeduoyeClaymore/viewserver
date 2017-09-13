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
import io.viewserver.core.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickc on 29/09/2014.
 */
public abstract class InputOperatorBase extends OperatorBase implements IInputOperator {
    private boolean allowDataReset;
    private final List<Runnable> deferredOperations = new ArrayList<>();

    protected InputOperatorBase(String name, ExecutionContext executionContext, ICatalog catalog) {
        super(name, executionContext, catalog);

    }

    @Override
    public void setAllowDataReset(boolean allowDataReset) {
        this.allowDataReset = allowDataReset;
    }

    @Override
    public void inputReady() {
        super.inputReady(null);
    }

    @Override
    protected void onSchemaClear() {
        // don't clear schema from an input operator
    }

    @Override
    protected void onDataClear() {
        // don't clear data from an input operator unless we've explicitly allowed it (e.g. writable tables)
        if (allowDataReset) {
            super.onDataClear();
            // this is here to cater for e.g. the table edit command handler, which may want to reset a writable table and then
            // add rows. Because the reset doesn't happen until the commit, we need to run those row operations AFTER the reset
            runDeferredOperations();
        }
    }

    @Override
    public void deferOperation(Runnable operation) {
        deferredOperations.add(operation);
    }

    private void runDeferredOperations() {
        int count = deferredOperations.size();
        for (int i = 0; i < count; i++) {
            deferredOperations.get(i).run();
        }
        deferredOperations.clear();
    }
}
