package io.viewserver.adapters;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import io.viewserver.adapters.common.Record;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseRecordLoader;
import io.viewserver.adapters.firebase.FirebaseTableUpdater;
import io.viewserver.adapters.firebase.FirebaseUtils;
import io.viewserver.catalog.Catalog;
import io.viewserver.core.ExecutionContext;
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
import io.viewserver.util.dynamic.NamedThreadFactory;
import org.junit.Test;
import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FireBaseRecordLoaderTest {

    private KeyedTable table;

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
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        table = new KeyedTable(tableName, executionContext, new Catalog(executionContext), ColumnHolderUtils.getSchema(config), new ChunkedColumnStorage(1024),new TableKeyDefinition("id"));
        table.initialise(100);
        loader.loadRecords(table);

        FirebaseTableUpdater tableUpdater = new FirebaseTableUpdater(connectionFactory);

        CountDownLatch latch = new CountDownLatch(1);
        getTableOutput().take(1).timeout(10, TimeUnit.SECONDS).subscribe(
                rec -> {
                    System.out.println(rec);
                    latch.countDown();
                }
        );
        String report_id = "report_id";
        tableUpdater.addOrUpdateRow(tableName,config, new Record().addValue("id", report_id).addValue("name","report_name"),(Integer)null);
        latch.await(10,TimeUnit.SECONDS);
    }

    private Observable<OperatorEvent> getTableOutput() {
        return table.getOutput().observable().filter(c->c.getEventType().equals(EventType.ROW_ADD));
    }




}

