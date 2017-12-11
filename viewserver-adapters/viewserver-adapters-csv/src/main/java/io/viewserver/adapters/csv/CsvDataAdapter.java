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

package io.viewserver.adapters.csv;

import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IDataAdapter;
import io.viewserver.datasource.IRecord;
import io.viewserver.schema.Schema;
import javolution.io.UTF8StreamReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.function.Consumer;

public class CsvDataAdapter implements IDataAdapter {
    private static final Logger log = LoggerFactory.getLogger(CsvDataAdapter.class);
    protected String fileName;
    protected int multiple;
    protected final CsvRecordWrapper recordWrapper;

    public CsvDataAdapter() {
        recordWrapper = getCsvRecordWrapper();
    }

    protected CsvRecordWrapper getCsvRecordWrapper() {
        return new CsvRecordWrapper(new DateTime(DateTimeZone.UTC));
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        recordWrapper.setDataSource(dataSource);
    }

    @Override
    public Schema getDerivedSchema() {
        throw new UnsupportedOperationException("You must specify the schema in the data source when using a CsvDataAdapter");
    }

    @Override
    public int getRecords(String query, Consumer<IRecord> consumer) {
        int recordsLoaded = 0;

        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
        recordsLoaded = this.getRecordsFromInputStream(stream, consumer);
        log.info(String.format("Loaded %s rows from %s", recordsLoaded, this.fileName));

        return recordsLoaded;
    }

    protected CSVFormat getCsvFormat(){
        return CSVFormat.EXCEL.withHeader();
    }

    protected int getRecordsFromInputStream(InputStream stream, Consumer<IRecord> consumer) {
        int recordsLoaded = 0;
        try {
            UTF8StreamReader reader = new UTF8StreamReader();
            reader.setInput(stream);
            try {
                CSVParser parser = getCsvFormat().parse(reader);

                for (final CSVRecord record : parser) {
                    for (int z = 0; z < multiple; z++) {
                        recordWrapper.setRecord(record);
                        consumer.accept(recordWrapper);
                        recordsLoaded++;
                        if (recordsLoaded % 100000 == 0) {
                            log.debug("{} records loaded", recordsLoaded);
                        }
                    }
                }
            } finally {
                reader.close();
            }

            log.info(String.format("Loaded %s rows from %s", recordsLoaded, this.fileName));
        } catch (Throwable e) {
            log.error(String.format("Failed to load CSV data from %s", this.fileName), e);
        }

        return recordsLoaded;
    }

    public CsvDataAdapter withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getMultiple() {
        return multiple;
    }

    public void setMultiple(int multiple) {
        this.multiple = multiple;
    }
}
