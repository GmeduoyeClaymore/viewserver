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

package io.viewserver.core;

import io.viewserver.catalog.MetadataRegistry;
import io.viewserver.configurator.Configurator;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.expression.AntlrExpressionParser;
import io.viewserver.expression.IExpressionParser;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.operators.IInputOperator;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.OperatorFactoryRegistry;
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.operators.spread.SpreadFunctionRegistry;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.ITask;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickc on 29/09/2014.
 */
public class ExecutionContext implements IExecutionContext{
    private static final Logger log = LoggerFactory.getLogger(ExecutionContext.class);
    private final List<IInputOperator> inputOperators = new ArrayList<>();
    private final List<IOperator> operators = new ArrayList<>();
    private final MetadataRegistry metadataRegistry;
    private final SummaryRegistry summaryRegistry;
    private final SpreadFunctionRegistry spreadColumnRegistry;
    private final DimensionMapper dimensionMapper;
    private ThreadLocal<Integer> executionCount = ThreadLocal.withInitial(() -> 0);
    private IReactor reactor;
    private boolean committing;
    private final List<IOperator> tearDownQueue = new ArrayList<>();
    private final FunctionRegistry functionRegistry;
    private final OperatorFactoryRegistry operatorFactoryRegistry;
    private final Configurator configurator;
    private IExpressionParser expressionParser;
    private boolean paused;
    private int numberThreads;
    public static boolean blockThreadAssertion = false;

    public static void AssertUpdateThread(){
        if(true){
            return;
        }
        if(!Thread.currentThread().getName().startsWith("reactor-")){
            throw new RuntimeException(String.format("This code is being called from a non reactor thread this is wrong \"%s\"",Thread.currentThread().getName()));
        }
    }

    public ExecutionContext(){
        this(1);
    }
    public ExecutionContext(int numberThreads) {
        this.numberThreads = numberThreads;
        this.functionRegistry = new FunctionRegistry();
        this.expressionParser = new AntlrExpressionParser(functionRegistry);
        this.summaryRegistry = new SummaryRegistry();
        this.spreadColumnRegistry = new SpreadFunctionRegistry();
        this.dimensionMapper = new DimensionMapper();
        this.operatorFactoryRegistry = new OperatorFactoryRegistry(dimensionMapper, this.spreadColumnRegistry, functionRegistry,() -> new ChunkedColumnStorage(1024), summaryRegistry);
        this.configurator = new Configurator(operatorFactoryRegistry);
        this.metadataRegistry = new MetadataRegistry();
    }

    @Override
    public DimensionMapper getDimensionMapper() {
        return dimensionMapper;
    }

    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    public OperatorFactoryRegistry getOperatorFactoryRegistry() {
        return operatorFactoryRegistry;
    }

    public Configurator getConfigurator() {
        return configurator;
    }

    public MetadataRegistry getMetadataRegistry() {
        return metadataRegistry;
    }

    public SummaryRegistry getSummaryRegistry() {
        return summaryRegistry;
    }

    @Override
    public SpreadFunctionRegistry getSpreadColumnRegistry() {
        return spreadColumnRegistry;
    }

    public void pause() {
        if (!paused) {
            // do a commit before pausing to make sure everything is up-to-date
            commit();

            log.debug("Execution context paused");
            paused = true;
        }
    }

    public void resume() {
        if (paused) {
            log.debug("Execution context resumed");
            paused = false;
            commit();
        }
    }

    public void commit() {
        if (paused) {
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("Beginning commit {}", executionCount);
        }
        committing = true;
        long start = System.currentTimeMillis();

        tearDownOperators();

        commitInputOperators();

        onAfterCommit();

        committing = false;
        if (log.isTraceEnabled()) {
            log.trace("Finished commit {}", executionCount);
        }

        long finish = System.currentTimeMillis();
        if (finish - start > 1000) {
            log.warn("Slow commit {} took {}ms", executionCount, finish - start);
        }

        executionCount.set(executionCount.get() + 1);
    }

    private void commitInputOperators() {
        int count = inputOperators.size();
        for (int i = 0; i < count; i++) {
            inputOperators.get(i).inputReady();
        }
    }

    private void onAfterCommit() {
        long start = System.nanoTime();
        int count = operators.size();
        for (int i = 0; i < count; i++) {
            operators.get(i).onAfterCommit();
        }
        if (log.isTraceEnabled()) {
            log.trace("Spent {}ms in onAfterCommit()", (System.nanoTime() - start) / 1000000f);
        }
    }



    @Override
    public void submit(Runnable work, int delay) {
        this.reactor.scheduleTask(new ITask() {
            @Override
            public void execute() {
                work.run();
            }
        },delay,0);
    }

    public void register(IOperator operator) {
        if (!operators.add(operator)) {
            throw new IllegalArgumentException("Operator already registered");
        }
        if (operator instanceof IInputOperator) {
            addInput((IInputOperator)operator);
        }
    }

    public void unregister(IOperator operator) {
        if (!operators.remove(operator)) {
            throw new IllegalArgumentException("Operator not registered");
        }
        if (operator instanceof IInputOperator) {
            removeInput((IInputOperator)operator);
        }
    }

    private void addInput(IInputOperator inputOperator) {
        log.trace("Adding input - {}",inputOperator);
        inputOperators.add(inputOperator);
    }

    private void removeInput(IInputOperator inputOperator) {
        log.trace("Removing input - {}",inputOperator);
        inputOperators.remove(inputOperator);
    }

    public int getExecutionCount() {
        return executionCount.get();
    }

    public void setReactor(IReactor reactor) {
        if (this.reactor != null) {
            throw new IllegalStateException("Cannot change the reactor once set");
        }
        this.reactor = reactor;
        this.reactor.addLoopTask(new Runnable() {
            @Override
            public void run() {
                commit();
            }
        });
    }

    public IReactor getReactor() {
        return reactor;
    }

    private void tearDownOperators() {
        ArrayList<IOperator> operators = new ArrayList<>(tearDownQueue);
        tearDownQueue.clear();
        int count = operators.size();
        for (int i = 0; i < count; i++) {
            operators.get(i).doTearDown();
        }
    }

    public void tearDownOperator(IOperator operator) {
        tearDownQueue.add(operator);
    }

    public IExpressionParser getExpressionParser() {
        return expressionParser;
    }
}
