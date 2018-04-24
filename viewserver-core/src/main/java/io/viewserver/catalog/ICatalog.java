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

import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.IOutput;

/**
 * Created by bemm on 07/10/2014.
 */
public interface ICatalog {
    String getName();

    IOutput getOutput();

    ICatalog getParent();

    IExecutionContext getExecutionContext();

    int registerOperator(IOperator operator);

    IOperator getOperator(String name);

    IOperator getOperatorByPath(String name);

    rx.Observable<IOperator> getOperatorObservable(String name);

    void unregisterOperator(IOperator operator);

    ICatalog createDescendant(String path);

    ICatalog getDescendant(String path);

    void addChild(ICatalog childCatalog);

    void removeChild(ICatalog childCatalog);

    rx.Observable<IOperator> waitForOperator(String name);

    rx.Observable<ICatalog> waitForChild(String name);

    java.util.Collection<IOperator> getAllOperators();

    ICatalog getChild(String name);

    void tearDown();

}
