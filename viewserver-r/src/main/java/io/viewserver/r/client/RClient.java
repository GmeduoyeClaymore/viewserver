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

package io.viewserver.r.client;

import io.viewserver.client.RowEvent;
import io.viewserver.client.SubscriptionEventHandlerBase;
import io.viewserver.client.ViewServerClient;
import io.viewserver.execution.Options;
import io.viewserver.network.IEndpoint;
import io.viewserver.subscription.SubscriptionFlags;
import org.rosuda.REngine.REXPLogical;

import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by nick on 11/07/15.
 */
public class RClient extends ViewServerClient {
    private static final byte[] logicalValues = new byte[]{REXPLogical.NA, REXPLogical.FALSE, REXPLogical.TRUE};

    public RClient(String name, IEndpoint endpoint) {
        super(name, endpoint);
    }

    public RClient(String name, String url) throws URISyntaxException {
        super(name, url);
    }

    public RClientSubscription subscribe(String operator, String output) {
        return subscribe(operator, output, null);
    }

    public RClientSubscription subscribe(String operator, String output, Options options) {
        try {
            if (options == null) {
                options = new Options();
                options.setOffset(-1);
                options.setLimit(-1);
            }
            options.setFlags(SubscriptionFlags.SnapshotOnly);

            CountDownLatch snapshotLatch = new CountDownLatch(1);
            Future<RClientSubscription> subscription = subscribe(operator, output, options, new SubscriptionEventHandlerBase<RClientSubscription>() {
                        @Override
                        public void onSnapshotComplete(RClientSubscription subscription) {
                            snapshotLatch.countDown();
                        }
                    },
                    (des, opt, cli) -> new RClientSubscription(des, cli)
            );
            snapshotLatch.await();

            return subscription.get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public boolean writeTable(String tableName, Object[] rowEventArray) throws ExecutionException, InterruptedException {
        ArrayList<RowEvent> rowEvents = new ArrayList<>();
        int numCols = rowEventArray.length;
        int numRows = Array.getLength(rowEventArray[0]);
        for (int i = 0; i < numRows; i++) {
            HashMap<Integer, Object> values = new HashMap<>();
            for (int j = 0; j < numCols; j++) {
                values.put(j, Array.get(rowEventArray[j], i));
            }
            rowEvents.add(RowEvent.addRow(values));
        }
        return editTable(tableName, rowEvents, true).get();
    }
}
