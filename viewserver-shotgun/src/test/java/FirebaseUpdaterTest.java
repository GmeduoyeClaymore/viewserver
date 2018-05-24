import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.servercomponents.DataSourceTableName;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.adapters.common.Record;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseRecordLoader;
import io.viewserver.adapters.firebase.FirebaseTableUpdater;
import io.viewserver.adapters.firebase.FirebaseUtils;
import io.viewserver.datasource.*;
import org.junit.Test;
import rx.Observable;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FirebaseUpdaterTest {

    @Test
    public void Can_listen_to_changes_on_user_table() throws InterruptedException {
        FirebaseConnectionFactory connectionFactory = new FirebaseConnectionFactory("firebase/shotgunServer.json");


        DataSourceTableName tableName = new DataSourceTableName(TableNames.USER_TABLE_NAME);

        Firestore db = connectionFactory.getConnection();

        CollectionReference collection = db.collection(tableName.getDataSourceName());
        FirebaseUtils.deleteCollection(collection);

        FirebaseRecordLoader loader = new FirebaseRecordLoader(connectionFactory, tableName.getDataSourceName(), UserDataSource.getDataSource().getSchema(), new OperatorCreationConfig(CreationStrategy.FAIL,CreationStrategy.FAIL));
        Observable<IRecord> recordObservable = loader.getRecords(null);

        CountDownLatch latch = new CountDownLatch(1);
        recordObservable.subscribe(
                rec -> {
                    System.out.println(rec);
                    latch.countDown();
                }
        );
        latch.await(300,TimeUnit.SECONDS);
    }
}
