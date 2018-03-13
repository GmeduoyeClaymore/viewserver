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
import io.viewserver.command.CommandResult;
import io.viewserver.core.IExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickc on 20/10/2014.
 */
public abstract class ConfigurableOperatorBase<TConfig> extends OperatorBase implements IConfigurableOperator<TConfig> {
    protected TConfig config;
    protected TConfig pendingConfig;
    protected List<CommandResult> pendingConfigResults = new ArrayList<>();

    protected ConfigurableOperatorBase(String name, IExecutionContext executionContext, ICatalog catalog) {
        super(name, executionContext, catalog);
    }

    @Override
    public final void configure(TConfig config, CommandResult configureResult) {
        if (this.pendingConfig != null) {
            try {
                this.pendingConfig = mergePendingConfig(pendingConfig, config);
            }catch (Exception ex){
                log.warn("Problem merging filter expression config",ex);
                this.pendingConfig = config;
            }
        } else {
            this.pendingConfig = config;
        }
        this.pendingConfigResults.add(configureResult.setSuccess(true));
        log.debug("Configured operator {} with config {} pending config is {}",this.getName(),config,this.pendingConfig);
    }

    protected TConfig mergePendingConfig(TConfig pendingConfig, TConfig config) {
        throw new UnsupportedOperationException("Operators of type " + this.getClass().getName() + " do not support merging of configs");
    }

    @Override
    protected void onProcessConfig() {
        if (pendingConfig != null) {
            try {
                processConfig(pendingConfig);
            } catch (Throwable e) {
                int count = pendingConfigResults.size();
                for (int i = 0; i < count; i++) {
                    pendingConfigResults.get(i).setSuccess(false).addMessage(e.getMessage()).setComplete(true);
                }
                pendingConfigResults.clear();
                throw e;
            }
        }
    }

    protected void setConfigFailed(String message) {
        int count = pendingConfigResults.size();
        for (int i = 0; i < count; i++) {
            pendingConfigResults.get(i).setSuccess(false).addMessage(message);
        }
    }

    @Override
    protected void onAfterSchema() {
        super.onAfterSchema();

        if (pendingConfig != null) {
            config = pendingConfig;
            pendingConfig = null;
            int count = pendingConfigResults.size();
            for (int i = 0; i < count; i++) {
                pendingConfigResults.get(i).setComplete(true);
            }
            pendingConfigResults.clear();
        }
    }

    protected void processConfig(TConfig config) {
    }
}
