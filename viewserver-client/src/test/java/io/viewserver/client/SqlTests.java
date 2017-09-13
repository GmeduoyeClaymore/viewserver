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

package io.viewserver.client;

import io.viewserver.BenchmarkTestBase;
import io.viewserver.execution.Options;
import io.viewserver.network.EndpointFactoryRegistry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by nick on 19/11/15.
 */
public class SqlTests extends BenchmarkTestBase {
    private static final Logger log = LoggerFactory.getLogger(SqlTests.class);

    @Test
    public void test() throws Throwable {
        ViewServerClient client = new ViewServerClient("client", EndpointFactoryRegistry.createEndpoint("tcp://localhost:18000"));
        Options options = new Options();
        options.setLimit(10);
        CountDownLatch snapshotLatch = new CountDownLatch(1);
        client.executeSql("select day, dv01, notional " +
                "from negotiationsDayCalCol n " +
//                "join (select day, rate_EUR from fxRatesFilter) f on f.day = n.day " +
//                "join fxRatesFilter r on r.day = n.day " +
                "where dv01 > 0 and day < 50 " +
//                "group by day " +
//                "having sum(dv01) > 0 " +
//                "order by day " +
                "limit 15", false, getSubscriptionEventHandler(null, snapshotLatch));
        snapshotLatch.await();
    }

    @Test
    public void test2() throws Throwable {
        ViewServerClient client = new ViewServerClient("client", EndpointFactoryRegistry.createEndpoint("tcp://localhost:18000"));
        Options options = new Options();
        options.setLimit(10);
        CountDownLatch snapshotLatch = new CountDownLatch(1);
        client.executeSql("select * from property where town='CLACTON-ON-SEA' limit 15", false, getSubscriptionEventHandler(null, snapshotLatch));
       // client.executeSql("select * from property where lat > 51 limit 15", false, getSubscriptionEventHandler(null, snapshotLatch));
        snapshotLatch.await();
    }


    private SubscriptionEventHandlerBase<ClientSubscription> getSubscriptionEventHandler(final Benchmarks benchmarks, final CountDownLatch snapshotLatch) {
        return new SubscriptionEventHandlerBase<ClientSubscription>() {
            @Override
            public void onDataReset(ClientSubscription subscription) {
                log.info("Data reset!");
            }

            @Override
            public void onSnapshotComplete(ClientSubscription subscription) {
                if (benchmarks != null) {
                    benchmarks.stopBenchmark("subscribing");
                }
                log.info("Snapsnot complete!");
                snapshotLatch.countDown();
            }

            @Override
            public void onBatchComplete(ClientSubscription subscription, int rowEventsCount) {
                subscription.printSnapshot();
            }

            @Override
            public void onRowAdd(ClientSubscription subscription, int rowId, Map<String, Object> values) {
                log.info("Row {} added", rowId);
            }

            @Override
            public void onRowUpdate(ClientSubscription subscription, int rowId, Map<String, Object> values) {
                log.info("Row {} updated", rowId);
            }

            @Override
            public void onRowRemove(ClientSubscription subscription, int rowId) {
                log.info("Row {} removed", rowId);
            }
        };
    }
}
