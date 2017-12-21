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

package io.viewserver.distribution;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.*;
import io.viewserver.schema.column.IRowFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Created by nickc on 08/12/2014.
 */
public class DistributionOperator extends ConfigurableOperatorBase<IDistributionConfig> {
    private static final Logger log = LoggerFactory.getLogger(DistributionOperator.class);
    private final Input input;
    private IDistributionManager distributionManager;
    private IDataDistributor dataDistributor;

    public DistributionOperator(String name, IExecutionContext executionContext, ICatalog catalog,
                                IDistributionManager distributionManager) {
        super(name, executionContext, catalog);
        this.distributionManager = distributionManager;

        input = new Input(Constants.IN, this);
        addInput(input);

        setSystemOperator(true);
    }

    public IInput getInput() {
        return input;
    }

    @Override
    protected void processConfig(IDistributionConfig config) {
        if (this.config != null
                && Objects.equals(this.config.getDistributionMode(), config.getDistributionMode())
                && Objects.equals(this.config.getStripingStrategy(), config.getStripingStrategy())) {
            return;
        }

        if (dataDistributor != null) {
            dataDistributor.tearDown();
            dataDistributor = null;
        }

        switch (config.getDistributionMode()) {
            case Mirrored: {
                dataDistributor = new MirroringDataDistributor(this, distributionManager);
                break;
            }
            case Striped: {
                dataDistributor = new StripingDataDistributor(this, distributionManager, config.getStripingStrategy());
                break;
            }
            default: {
                throw new OperatorConfigurationException(this,
                        new IllegalArgumentException("Distribution operator cannot run in 'local' mode"));
            }
        }
    }

    @Override
    protected void commit() {
        if (dataDistributor != null) {
            dataDistributor.onBeforeCommit();
        }

        super.commit();
    }

    @Override
    public void onAfterCommit() {
        super.onAfterCommit();

        if (dataDistributor != null) {
            dataDistributor.onAfterCommit();
        }
    }

    @Override
    public void doTearDown() {
        if (dataDistributor != null) {
            dataDistributor.tearDown();
            dataDistributor = null;
        }

        super.doTearDown();
    }

    private class Input extends InputBase {
        private int rowAdds;

        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public void onData() {
            log.trace("{} in onData - producer has {} rows", getOwner().getPath(), getProducer().getRowCount());
            rowAdds = 0;
            super.onData();
            log.trace("{} had {} row adds", getOwner().getPath(), rowAdds);
        }

        @Override
        protected void onRowAdd(int row) {
            dataDistributor.onRowAdd(row);
            rowAdds++;
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            dataDistributor.onRowUpdate(row, rowFlags);
        }

        @Override
        protected void onRowRemove(int row) {
            dataDistributor.onRowRemove(row);
        }
    }
}
