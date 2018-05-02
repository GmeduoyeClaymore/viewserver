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

import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.IRecordLoader;
import io.viewserver.datasource.OperatorCreationConfig;
import io.viewserver.datasource.SchemaConfig;
import javolution.io.UTF8StreamReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.InputStream;
import java.util.function.Consumer;

public class CsvRecordLoader implements IRecordLoader {
    private static final Logger log = LoggerFactory.getLogger(CsvRecordLoader.class);
    protected String fileName;
    protected int multiple = 1;
    protected final CsvRecordWrapper recordWrapper;
    private SchemaConfig schemaConfig;
    private OperatorCreationConfig creationConfig;

    public CsvRecordLoader(SchemaConfig schemaConfig, OperatorCreationConfig creationConfig) {
        this.schemaConfig = schemaConfig;
        this.creationConfig = creationConfig;
        recordWrapper = getCsvRecordWrapper(schemaConfig);
    }

    protected CsvRecordWrapper getCsvRecordWrapper(SchemaConfig schemaConfig) {
        return new CsvRecordWrapper(new DateTime(DateTimeZone.UTC), schemaConfig);
    }

    public SchemaConfig getSchemaConfig() {
        return schemaConfig;
    }


    @Override
    public OperatorCreationConfig getCreationConfig() {
        return creationConfig;
    }

    @Override
    public void close() {

    }

    @Override
    public rx.Observable<IRecord> getRecords(String query) {
        return  rx.Observable.create(subscriber -> {
        try{
            InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
            this.getRecordsFromInputStream(stream, c -> subscriber.onNext(c));
            subscriber.onCompleted();
        }catch (Exception ex){
            subscriber.onError(ex);
        }}, Emitter.BackpressureMode.BUFFER);
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
                        log.debug("{} records loaded", recordsLoaded);
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

    public CsvRecordLoader withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
}
