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

package io.viewserver.adapters.common;

import io.viewserver.datasource.ColumnType;
import io.viewserver.datasource.IDataAdapter;
import io.viewserver.datasource.IRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by nick on 15/09/15.
 */
public class PollingDataLoader extends DataLoader {
    private static final Logger log = LoggerFactory.getLogger(PollingDataLoader.class);
    private final String deltaFieldName;
    private final ColumnType deltaFieldType;
    private int interval;
    private IDeltaValueTracker deltaValueTracker;
    private boolean intervalExact;
    private IPollingDataQueryProvider dataQueryProvider;

    public PollingDataLoader(String name, String deltaFieldName, ColumnType deltaFieldType, int interval,
                             boolean intervalExact, IDataAdapter dataAdapter, IPollingDataQueryProvider dataQueryProvider) {
        super(name, dataAdapter, dataQueryProvider);
        this.interval = interval;
        this.deltaFieldName = deltaFieldName;
        this.deltaFieldType = deltaFieldType;
        this.intervalExact = intervalExact;
        this.dataQueryProvider = dataQueryProvider;

        initialiseDeltaValueTracker(deltaFieldName, deltaFieldType);
    }

    private void initialiseDeltaValueTracker(String deltaFieldName, ColumnType deltaFieldType) {
        switch (deltaFieldType) {
            case Int: {
                deltaValueTracker = new IntDeltaValueTracker(deltaFieldName);
                break;
            }
            case DateTime: {
                deltaValueTracker = new TimestampDeltaValueTracker(deltaFieldName);
                break;
            }
            default: {
                throw new UnsupportedOperationException(String.format("No delta value tracker for column type '%s'", deltaFieldType));
            }
        }
    }

    public int getInterval() {
        return interval;
    }

    public boolean getIntervalExact() {
        return intervalExact;
    }

    public String getDeltaFieldName() {
        return deltaFieldName;
    }

    public ColumnType getDeltaFieldType() {
        return deltaFieldType;
    }

    @Override
    protected boolean loadData() {
        super.loadData();

        while (true) {
            try {
                final long interval;
                if (intervalExact) {
                    // for exact interval - e.g. every 1000ms, on the second; every 60000ms, on the minute; etc
                    long now = System.currentTimeMillis();
                    interval = this.interval - (now % this.interval);
                } else {
                    interval = this.interval;
                }
                log.debug("DataSource: " + this.name + " will poll in " + interval + " millis");
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                log.error("Polling data adapter was interrupted between polls");
                return false;
            }
            log.debug("DataSource: " + this.name + " loading deltas");
            loadDelta();
            processPendingRecords(false);
        }
    }

    private void loadDelta() {
        dataAdapter.getRecords(dataQueryProvider.getDeltaQuery(deltaValueTracker.getLastDeltaValue()),
                record -> {
                    this.addOrUpdateRecord(record, false);
                } );
    }

    @Override
    protected void addOrUpdateRecord(IRecord record, boolean isSnapshot) {
        deltaValueTracker.checkDeltaValue(record);
        super.addOrUpdateRecord(record, isSnapshot);
    }

    private interface IDeltaValueTracker {
        Object getLastDeltaValue();
        void checkDeltaValue(IRecord record);
    }

    private static class IntDeltaValueTracker implements IDeltaValueTracker {
        private final String deltaFieldName;
        private int lastDeltaValue = Integer.MIN_VALUE;

        public IntDeltaValueTracker(String deltaFieldName) {
            this.deltaFieldName = deltaFieldName;
        }

        @Override
        public Object getLastDeltaValue() {
            return lastDeltaValue;
        }

        @Override
        public void checkDeltaValue(IRecord record) {
            int newValue = record.getInt(deltaFieldName);
            if (lastDeltaValue == Integer.MIN_VALUE || newValue > lastDeltaValue) {
                lastDeltaValue = newValue;
            }
        }
    }

    private static class TimestampDeltaValueTracker implements IDeltaValueTracker {
        private final String deltaFieldName;
        private Date lastDeltaValue;

        public TimestampDeltaValueTracker(String deltaFieldName) {
            this.deltaFieldName = deltaFieldName;
        }

        @Override
        public Object getLastDeltaValue() {
            return lastDeltaValue;
        }

        @Override
        public void checkDeltaValue(IRecord record) {
            Date newValue = record.getDateTime(deltaFieldName);
            if (lastDeltaValue == null || newValue.after(lastDeltaValue)) {
                lastDeltaValue = newValue;
            }
        }
    }
}
