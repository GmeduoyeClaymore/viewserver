package io.viewserver.adapters;

import com.fasterxml.jackson.databind.Module;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.viewserver.adapters.common.Record;
import io.viewserver.adapters.mongo.MongoConnectionFactory;
import io.viewserver.adapters.mongo.MongoRecordLoader;
import io.viewserver.adapters.mongo.MongoTableUpdater;
import io.viewserver.catalog.Catalog;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.*;
import io.viewserver.operators.rx.EventType;
import io.viewserver.operators.rx.OperatorEvent;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.IReactorCommandWheel;
import io.viewserver.reactor.ITask;
import io.viewserver.reactor.SimpleReactorCommandWheel;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.DynamicSerializationModule;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import io.viewserver.util.dynamic.NamedThreadFactory;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Arrays.asList;

@Ignore
public class MongoRecordLoaderTest {

    static DynamicSerializationModule orderSerializationModule = new DynamicSerializationModule();

    static{
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        orderSerializationModule
                });
    }


    private String   clientURI = "mongodb+srv://development_user:Welcome123XXX@cluster0-mvv2v.mongodb.net";
    private MongoConnectionFactory connectionFactory;
    private String tableName;
    private SchemaConfig config;
    private MongoTableUpdater tableUpdater;
    private KeyedTable table;

    public void setup() {
        connectionFactory = new MongoConnectionFactory(clientURI, "test");

        tableName = "test_table_2";
        config = new SchemaConfig().withColumns(asList(
                new Column("id", ContentType.String),
                new Column("name", ContentType.Json)

        )).withKeyColumns("id");


        MongoDatabase db = connectionFactory.getConnection();

        MongoCollection<Document> collection = db.getCollection(tableName);
        collection.drop();
        db.createCollection(tableName);

        MongoRecordLoader loader = new MongoRecordLoader(connectionFactory, tableName, config, new OperatorCreationConfig(CreationStrategy.FAIL,CreationStrategy.FAIL),"main");
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        table = new KeyedTable(tableName, executionContext, new Catalog(executionContext), ColumnHolderUtils.getSchema(config), new ChunkedColumnStorage(1024),new TableKeyDefinition("id"));
        table.initialise(100);
        loader.loadRecords(table);

        tableUpdater = new MongoTableUpdater(connectionFactory);
    }


    @Test
    public void Can_add_record_and_listen_for_changes() throws InterruptedException {

        setup();
        CountDownLatch latch = new CountDownLatch(1);
        getTableOutput().timeout(10, TimeUnit.SECONDS).subscribe(
                rec -> {
                    System.out.println(rec);
                    latch.countDown();
                },
                err -> {
                    System.out.println(err);
                }
        );
        int counter = 0;
        tableUpdater.addOrUpdateRow(tableName, config, new Record().addValue("id", "record_1_id" + counter++).addValue("name", "report_name"),null).subscribe();
        Assert.assertTrue(latch.await(10,TimeUnit.SECONDS));
    }

    private Observable<OperatorEvent> getTableOutput() {
        return table.getOutput().observable().filter(c->c.getEventType().equals(EventType.ROW_ADD));
    }


    @Test
    @Ignore
    public void Can_update_record_and_listen_for_changes() throws InterruptedException {

        setup();
        AtomicReference<String> name = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(2);
        getTableOutput().timeout(10, TimeUnit.SECONDS).subscribe(
                rec -> {
                    System.out.println(rec);
                    name.set((String) ((HashMap)rec.getEventData()).get("name"));
                    latch.countDown();
                },
                err -> {
                    System.err.println(err);
                }
        );
        tableUpdater.addOrUpdateRow(tableName, config, new Record().addValue("id", "record_1_id").addValue("name", "report_name"),null).subscribe();
        tableUpdater.addOrUpdateRow(tableName, config, new Record().addValue("id", "record_1_id").addValue("name", "report_name2"),null).subscribe();
        Assert.assertTrue(latch.await(2,TimeUnit.SECONDS));
        Assert.assertEquals("report_name2", name.get());
    }


    @Test
    public void Can_add_dynamic_json_backed_object() throws InterruptedException {

        setup();
        CountDownLatch latch = new CountDownLatch(1);
        getTableOutput().timeout(10, TimeUnit.SECONDS).subscribe(
                rec -> {
                    System.out.println(rec);
                    latch.countDown();
                },
                err -> {
                    System.out.println(err);
                }
        );
        int counter = 0;
        DynamicJsonBackedObject obj = JSONBackedObjectFactory.create(DynamicJsonBackedObject.class);
        obj.set("foo","bar");
        tableUpdater.addOrUpdateRow(tableName, config, new Record().addValue("id", "record_1_id" + counter++).addValue("name", obj),null).subscribe();
        Assert.assertTrue(latch.await(20,TimeUnit.SECONDS));
    }

    @Test
    public void Can_add_dynamic_json_backed_object_array() throws InterruptedException {

        setup();
        CountDownLatch latch = new CountDownLatch(1);
        getTableOutput().timeout(10, TimeUnit.SECONDS).subscribe(
                rec -> {
                    System.out.println(rec);
                    latch.countDown();
                },
                err -> {
                    System.out.println(err);
                }
        );
        int counter = 0;
        DynamicJsonBackedObject[] ratings = new DynamicJsonBackedObject[2];
        DynamicJsonBackedObject obj = JSONBackedObjectFactory.create(DynamicJsonBackedObject.class);
        obj.set("foo","bar");
        ratings[0] = obj;
        ratings[1] = obj;
        tableUpdater.addOrUpdateRow(tableName, config, new Record().addValue("id", "record_1_id" + counter++).addValue("name", ratings),null).subscribe();
        Assert.assertTrue(latch.await(20,TimeUnit.SECONDS));
    }







}


