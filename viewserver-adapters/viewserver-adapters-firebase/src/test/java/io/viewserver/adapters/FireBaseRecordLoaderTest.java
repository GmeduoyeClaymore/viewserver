package io.viewserver.adapters;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import io.viewserver.adapters.common.Record;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseRecordLoader;
import io.viewserver.adapters.firebase.FirebaseTableUpdater;
import io.viewserver.adapters.firebase.FirebaseUtils;
import io.viewserver.datasource.*;
import io.viewserver.report.ReportDefinition;
import org.junit.Test;
import rx.Observable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FireBaseRecordLoaderTest {

    @Test
    public void Can_add_record_and_listen_for_changes() throws InterruptedException {
        FirebaseConnectionFactory connectionFactory = new FirebaseConnectionFactory("firebase/shotgunServer.json");

        String tableName = "test_table";
        SchemaConfig config = new SchemaConfig().withColumns(Arrays.asList(
                new Column("id", ContentType.String),
                new Column("name", ContentType.String)

        )).withKeyColumns("id");



        Firestore db = connectionFactory.getConnection();

        CollectionReference collection = db.collection(tableName);
        FirebaseUtils.deleteCollection(collection);

        FirebaseRecordLoader loader = new FirebaseRecordLoader(connectionFactory, tableName, config, new OperatorCreationConfig(CreationStrategy.FAIL,CreationStrategy.FAIL));
        Observable<IRecord> recordObservable = loader.getRecords(null);

        FirebaseTableUpdater tableUpdater = new FirebaseTableUpdater(connectionFactory);

        CountDownLatch latch = new CountDownLatch(1);
        recordObservable.take(1).timeout(10, TimeUnit.SECONDS).subscribe(
                rec -> {
                    System.out.println(rec);
                    latch.countDown();
                }
        );
        String report_id = "report_id";
        tableUpdater.addOrUpdateRow(tableName,config, new Record().addValue("id", report_id).addValue("name","report_name"),null);
        latch.await(10,TimeUnit.SECONDS);
    }



}
