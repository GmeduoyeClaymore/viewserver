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

import java.util.List;
import java.util.Map;

/**
 * Created by nickc on 23/09/2014.
 */
public interface IOperator {
    String getName();

    IInput getInput(String name);

    Map<String, IInput> getInputs();

    IOutput getOutput(String name);

    List<IOutput> getOutputs();

    void inputReady(IInput input);

    void resetSchema();

    void resetData();

    void refreshData();

    void tearDown();

    String getPath();

    ExecutionContext getExecutionContext();

    void onAfterCommit();

    void doTearDown();

    ICatalog getCatalog();

    boolean isSystemOperator();

    void setSystemOperator(boolean isSystemOperator);

    void setMetadata(String key, Object value);

    Object getMetadata(String key);
}
