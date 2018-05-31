package io.viewserver.adapters;

import com.fasterxml.jackson.databind.Module;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import io.viewserver.adapters.common.Record;
import io.viewserver.adapters.mongo.MongoConnectionFactory;
import io.viewserver.adapters.mongo.MongoRecordLoader;
import io.viewserver.adapters.mongo.MongoTableUpdater;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.*;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.DynamicSerializationModule;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;

import java.util.concurrent.CountDownLatch;
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


    private String   clientURI = "mongodb+srv://development_user:Welcome123XXX@cluster0-62kku.mongodb.net";
    private MongoConnectionFactory connectionFactory;
    private String tableName;
    private SchemaConfig config;
    private MongoTableUpdater tableUpdater;
    private Observable<IRecord> recordObservable;

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

        MongoRecordLoader loader = new MongoRecordLoader(connectionFactory, tableName, config, new OperatorCreationConfig(CreationStrategy.FAIL,CreationStrategy.FAIL));
        recordObservable = loader.getRecords(null);

        tableUpdater = new MongoTableUpdater(connectionFactory);
    }


    @Test
    public void Can_add_record_and_listen_for_changes() throws InterruptedException {

        setup();
        CountDownLatch latch = new CountDownLatch(1);
        recordObservable.timeout(10, TimeUnit.SECONDS).subscribe(
                rec -> {
                    System.out.println(rec);
                    latch.countDown();
                },
                err -> {
                    System.out.println(err);
                }
        );
        int counter = 0;
        tableUpdater.addOrUpdateRow(tableName, config, new Record().addValue("id", "record_1_id" + counter++).addValue("name", "report_name")).subscribe();
        Assert.assertTrue(latch.await(10,TimeUnit.SECONDS));
    }


    @Test
    @Ignore
    public void Can_update_record_and_listen_for_changes() throws InterruptedException {

        setup();
        AtomicReference<String> name = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(2);
        recordObservable.timeout(10, TimeUnit.SECONDS).subscribe(
                rec -> {
                    System.out.println(rec);
                    name.set(rec.getString("name"));
                    latch.countDown();
                },
                err -> {
                    System.err.println(err);
                }
        );
        tableUpdater.addOrUpdateRow(tableName, config, new Record().addValue("id", "record_1_id").addValue("name", "report_name")).subscribe();
        tableUpdater.addOrUpdateRow(tableName, config, new Record().addValue("id", "record_1_id").addValue("name", "report_name2")).subscribe();
        Assert.assertTrue(latch.await(2,TimeUnit.SECONDS));
        Assert.assertEquals("report_name2", name.get());
    }


    @Test
    public void Can_add_dynamic_json_backed_object() throws InterruptedException {

        setup();
        CountDownLatch latch = new CountDownLatch(1);
        recordObservable.timeout(10, TimeUnit.SECONDS).subscribe(
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
        tableUpdater.addOrUpdateRow(tableName, config, new Record().addValue("id", "record_1_id" + counter++).addValue("name", obj)).subscribe();
        Assert.assertTrue(latch.await(20,TimeUnit.SECONDS));
    }



    @Test
    public void Can_add_dynamic_json_backed_object_array() throws InterruptedException {

        setup();
        CountDownLatch latch = new CountDownLatch(1);
        recordObservable.timeout(10, TimeUnit.SECONDS).subscribe(
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
        tableUpdater.addOrUpdateRow(tableName, config, new Record().addValue("id", "record_1_id" + counter++).addValue("name", ratings)).subscribe();
        Assert.assertTrue(latch.await(20,TimeUnit.SECONDS));
    }







}
