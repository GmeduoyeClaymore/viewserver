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

package io.viewserver.server;

import io.viewserver.BenchmarkTestBase;
import io.viewserver.catalog.Catalog;
import io.viewserver.client.ClientSubscription;
import io.viewserver.client.SubscriptionEventHandlerBase;
import io.viewserver.command.CommandHandlerRegistry;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.execution.Options;
import io.viewserver.execution.ReportContext;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.messages.protobuf.Message;
import io.viewserver.network.Network;
import io.viewserver.network.netty.tcp.NettyTcpEndpoint;
import io.viewserver.operators.filter.FilterOperator;
import io.viewserver.reactor.EventLoopReactor;
import io.viewserver.server.steps.TestViewServerClient;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

@Ignore
public class EventLoopReactorTest extends BenchmarkTestBase {
    private static final Logger log = LoggerFactory.getLogger(EventLoopReactorTest.class);

    public void testControls() throws Exception {
        EventLoopReactor reactor = new EventLoopReactor("reactor", null);

        reactor.start();

        Thread.sleep(1000);
        reactor.wakeUp();

        Thread.sleep(10000);
        reactor.shutDown();
    }

    @Before
    public void setup() throws Exception {
        /*log.info("Loading properties file {}", "viewserver-core/src/main/resources/properties/viewserver.dev.properties");
        try (InputStream reader = Files.newInputStream(Paths.get("viewserver-core/src/main/resources/properties/viewserver.dev.properties"), StandardOpenOption.READ)) {
            System.getProperties().load(reader);
        }*/
    }

    private void loadClasses() throws Exception {
        String userDir = System.getProperty("user.dir");
        loadClasses(userDir + "\\viewserver-engine-core\\target\\classes");
    }

    private void loadClasses(String directory) throws ClassNotFoundException, IOException {
        final ClassLoader classLoader = this.getClass().getClassLoader();

        classLoader.loadClass("io.viewserver.expression.parser.ExpressionVisitorImpl");
        classLoader.loadClass("io.viewserver.expression.tree.divide.DivideExpression");

        Path path = Paths.get(directory);
        Files.walkFileTree(path, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".class") && !file.toString().contains("_KeyName_")) {
                    String[] split = file.toString().split("\\\\");
                    String className = "";
                    for (int i = split.length - 1; i >= 0; i--) {
                        if (split[i].equals("classes")) {
                            break;
                        }
                        className = split[i] + "." + className;
                    }
                    className = className.substring(0, className.length() - 7);
                    try {
                        classLoader.loadClass(className);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Test
    public void subscribeDimensionTest() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(final Benchmarks benchmarks) throws Exception {
                new Message();

                ReportContext reportContext = new ReportContext();
                reportContext.setReportName("default");
                reportContext.getParameterValues().put("aggregators", ValueLists.valueListOf("client"));
                reportContext.getParameterValues().put("measures", ValueLists.valueListOf("hitRate"));
                reportContext.getParameterValues().put("bucketSize", ValueLists.valueListOf(7));
                reportContext.getParameterValues().put("buckets", ValueLists.valueListOf(1));
                reportContext.getParameterValues().put("startBucket", ValueLists.valueListOf(0));
                reportContext.getParameterValues().put("basis", ValueLists.valueListOf("dv01"));
                reportContext.getParameterValues().put("currency", ValueLists.valueListOf("USD"));

                Options options = new Options();
                options.setOffset(0);
                options.setLimit(2);

                subscribeDimension("traded", reportContext, options, benchmarks, false, false);
            }
        }, 1);
    }

    @Test
    public void blahTest() throws Exception {
        try (TestViewServerClient client = new TestViewServerClient("client", new NettyTcpEndpoint("tcp://localhost:18000"))) {
            Options options = new Options();
            options.setOffset(0);
            options.setLimit(1000);

            CountDownLatch latch = new CountDownLatch(1);
            Future<ClientSubscription> subscriptionFuture = client.subscribe("viewserver-csv_index", "btyd", options, new SubscriptionEventHandlerBase<ClientSubscription>() {
                @Override
                public void onSnapshotComplete(ClientSubscription clientSubscription) {
                    latch.countDown();
                }
            });
            latch.await();
            subscriptionFuture.get().printSnapshot();
        }
    }

    @Test
    public void userDataTest() throws Exception {
        try (TestViewServerClient client = new TestViewServerClient("client", new NettyTcpEndpoint("tcp://localhost:18000"))) {
            client.authenticate("open", "admin@viewserver.com");
            Thread.sleep(3000);

//            client.editTable("userdata", Arrays.asList(
//                    RowEvent.addRow(new HashMap<Integer, Object>() {{
//                        put(0, "data");
//                        put(1, "the value");
//                    }})
//            ), false);

            Options options = new Options();
            options.setOffset(0);
            options.setLimit(1000);

            Future<ClientSubscription> subscriptionFuture = client.subscribe("userdata", options, null);
            try (ClientSubscription subscription = subscriptionFuture.get()) {
                subscription.getSnapshot().get();
                subscription.printSnapshot();
            }
        }
    }

    @Test
    public void subscribeReportTest() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(final Benchmarks benchmarks) throws Exception {
                new Message();

                ReportContext inquiriesContext1 = new ReportContext();
                inquiriesContext1.setReportName("simple");
                inquiriesContext1.getParameterValues().put("aggregators", ValueLists.valueListOf("client"));
                inquiriesContext1.getParameterValues().put("measures", ValueLists.valueListOf("sum"));
                inquiriesContext1.getParameterValues().put("bucketSize", ValueLists.valueListOf(7));
                inquiriesContext1.getParameterValues().put("buckets", ValueLists.valueListOf(1));
                inquiriesContext1.getParameterValues().put("startBucket", ValueLists.valueListOf(0));
                inquiriesContext1.getParameterValues().put("basis", ValueLists.valueListOf("tickets"));
                inquiriesContext1.getParameterValues().put("currency", ValueLists.valueListOf("USD"));

                ReportContext hitRateContext1 = new ReportContext();
                hitRateContext1.setReportName("default");
                hitRateContext1.getParameterValues().put("aggregators", ValueLists.valueListOf("client"));
                hitRateContext1.getParameterValues().put("measures", ValueLists.valueListOf("hitRate"));
                hitRateContext1.getParameterValues().put("bucketSize", ValueLists.valueListOf(7));
                hitRateContext1.getParameterValues().put("buckets", ValueLists.valueListOf(1));
                hitRateContext1.getParameterValues().put("startBucket", ValueLists.valueListOf(0));
                hitRateContext1.getParameterValues().put("basis", ValueLists.valueListOf("dv01"));
                hitRateContext1.getParameterValues().put("currency", ValueLists.valueListOf("USD"));

                inquiriesContext1.getDimensionValues().add(new ReportContext.DimensionValue("client", false, "Bygrave Capital"));
                hitRateContext1.getDimensionValues().add(new ReportContext.DimensionValue("client", false, "Bygrave Capital"));

                Options options = new Options();
                options.setOffset(0);
                options.setLimit(1000);
//                options.addSortColumn("clientTrader", false);

                subscribeReport(hitRateContext1, options, benchmarks, true, true);

                Thread.sleep(5000);
            }
        }, 10, false);
    }

    @Test
    public void subscribeBlotterTest() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(final Benchmarks benchmarks) throws Exception {
                ReportContext reportContext = new ReportContext();
                reportContext.setReportName("blotter");

                Options options = new Options();
                options.setOffset(0);
                options.setLimit(100);
                options.addSortColumn("timeStamp", true);

                subscribeReport(reportContext, options, benchmarks, true, false);
            }
        }, 1, false);
    }

    @Test
    public void subscribeRuleOutput() throws Exception {
        new Message();
        subscribeOperator("262d9d3ba297f758a77a0e60784b990914325d8f", null, true, false, false);
    }

    @Test
    public void subscribeMultiContextJoinTest() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(final Benchmarks benchmarks) throws Exception {
                ReportContext reportContext = new ReportContext();
                reportContext.setMultiContextMode("join");

                ReportContext childContext1 = new ReportContext();
                childContext1.setReportName("simple");
                childContext1.getParameterValues().put("aggregators", ValueLists.valueListOf("client"));
                childContext1.getParameterValues().put("measures", ValueLists.valueListOf("sum"));
                childContext1.getParameterValues().put("bucketSize", ValueLists.valueListOf(7));
                childContext1.getParameterValues().put("buckets", ValueLists.valueListOf(1));
                childContext1.getParameterValues().put("startBucket", ValueLists.valueListOf(0));
                childContext1.getParameterValues().put("basis", ValueLists.valueListOf("tickets"));

//                ReportContext childContext2 = new ReportContext(childContext1);
                ReportContext childContext2 = new ReportContext();
                childContext2.setReportName("default");
                childContext2.getParameterValues().put("aggregators", ValueLists.valueListOf("client"));
                childContext2.getParameterValues().put("measures", ValueLists.valueListOf("hitRate"));
                childContext2.getParameterValues().put("bucketSize", ValueLists.valueListOf(7));
                childContext2.getParameterValues().put("buckets", ValueLists.valueListOf(1));
                childContext2.getParameterValues().put("startBucket", ValueLists.valueListOf(0));
                childContext2.getParameterValues().put("basis", ValueLists.valueListOf("dv01"));

//                childContext1.getDimensionValues().add(new ReportContext.DimensionValue("client", false, "Bygrave Capital"));
//
//                childContext2.getDimensionValues().add(new ReportContext.DimensionValue("client", false, "BNP Asst Mgmt"));

                reportContext.getChildContexts().add(childContext1);
                reportContext.getChildContexts().add(childContext2);

                Options options = new Options();
                options.setOffset(0);
                options.setLimit(10);

                subscribeReport(reportContext, options, benchmarks, true, false);
            }
        }, 1);
    }

    @Test
    public void subscribeMultiContextUnionTransposeTest() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(final Benchmarks benchmarks) throws Exception {
                ReportContext reportContext = new ReportContext();
                reportContext.setMultiContextMode("uniontranspose");

                ReportContext childContext1 = new ReportContext();
                childContext1.setReportName("simple");
                childContext1.getParameterValues().put("aggregators", ValueLists.valueListOf("client"));
                childContext1.getParameterValues().put("measures", ValueLists.valueListOf("sum"));
                childContext1.getParameterValues().put("bucketSize", ValueLists.valueListOf(7));
                childContext1.getParameterValues().put("buckets", ValueLists.valueListOf(1));
                childContext1.getParameterValues().put("startBucket", ValueLists.valueListOf(0));
                childContext1.getParameterValues().put("basis", ValueLists.valueListOf("tickets"));

                ReportContext childContext2 = new ReportContext(childContext1);

                childContext1.getDimensionValues().add(new ReportContext.DimensionValue("client", false, "Bygrave Capital"));

                childContext2.getDimensionValues().add(new ReportContext.DimensionValue("client", false, "BNP Asst Mgmt"));

                reportContext.getChildContexts().add(childContext1);
                reportContext.getChildContexts().add(childContext2);

                Options options = new Options();
                options.setOffset(0);
                options.setLimit(10);

                subscribeReport(reportContext, options, benchmarks, true, false);
            }
        }, 1);
    }

    @Test
    public void subscribeMultiContextUnionGroupTest() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(final Benchmarks benchmarks) throws Exception {
                ReportContext reportContext = new ReportContext();
                reportContext.setMultiContextMode("uniongroup");

                ReportContext childContext1 = new ReportContext();
                childContext1.setReportName("simple");
                childContext1.getParameterValues().put("aggregators", ValueLists.valueListOf("client"));
                childContext1.getParameterValues().put("measures", ValueLists.valueListOf("sum"));
                childContext1.getParameterValues().put("bucketSize", ValueLists.valueListOf(7));
                childContext1.getParameterValues().put("buckets", ValueLists.valueListOf(1));
                childContext1.getParameterValues().put("startBucket", ValueLists.valueListOf(0));
                childContext1.getParameterValues().put("basis", ValueLists.valueListOf("tickets"));

                ReportContext childContext2 = new ReportContext(childContext1);

                childContext1.getDimensionValues().add(new ReportContext.DimensionValue("client", false, "Bygrave Capital"));

                childContext2.getDimensionValues().add(new ReportContext.DimensionValue("client", false, "BNP Asst Mgmt"));

                reportContext.getChildContexts().add(childContext1);
                reportContext.getChildContexts().add(childContext2);

                Options options = new Options();
                options.setOffset(0);
                options.setLimit(10);

                subscribeReport(reportContext, options, benchmarks, true, false);
            }
        }, 1);
    }

    @Test
    public void subscribeMultiContextUnionTransposeJoinTest() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(final Benchmarks benchmarks) throws Exception {
                ReportContext inquiriesContext1 = new ReportContext();
                inquiriesContext1.setReportName("simple");
                inquiriesContext1.getParameterValues().put("aggregators", ValueLists.valueListOf("client"));
                inquiriesContext1.getParameterValues().put("measures", ValueLists.valueListOf("sum"));
                inquiriesContext1.getParameterValues().put("bucketSize", ValueLists.valueListOf(7));
                inquiriesContext1.getParameterValues().put("buckets", ValueLists.valueListOf(1));
                inquiriesContext1.getParameterValues().put("startBucket", ValueLists.valueListOf(0));
                inquiriesContext1.getParameterValues().put("basis", ValueLists.valueListOf("tickets"));

                ReportContext hitRateContext1 = new ReportContext();
                hitRateContext1.setReportName("default");
                hitRateContext1.getParameterValues().put("aggregators", ValueLists.valueListOf("client"));
                hitRateContext1.getParameterValues().put("measures", ValueLists.valueListOf("hitRate"));
                hitRateContext1.getParameterValues().put("bucketSize", ValueLists.valueListOf(7));
                hitRateContext1.getParameterValues().put("buckets", ValueLists.valueListOf(1));
                hitRateContext1.getParameterValues().put("startBucket", ValueLists.valueListOf(0));
                hitRateContext1.getParameterValues().put("basis", ValueLists.valueListOf("dv01"));

                ReportContext inquiriesContext2 = new ReportContext(inquiriesContext1);

                ReportContext hitRateContext2 = new ReportContext(hitRateContext1);

                inquiriesContext1.getDimensionValues().add(new ReportContext.DimensionValue("client", false, "Bygrave Capital"));
                hitRateContext1.getDimensionValues().add(new ReportContext.DimensionValue("client", false, "Bygrave Capital"));

                inquiriesContext2.getDimensionValues().add(new ReportContext.DimensionValue("client", false, "BNP Asst Mgmt"));
                hitRateContext2.getDimensionValues().add(new ReportContext.DimensionValue("client", false, "BNP Asst Mgmt"));

                ReportContext joinContext1 = new ReportContext();
                joinContext1.setMultiContextMode("join");
                joinContext1.getChildContexts().add(inquiriesContext1);
                joinContext1.getChildContexts().add(hitRateContext1);

                ReportContext joinContext2 = new ReportContext();
                joinContext2.setMultiContextMode("join");
                joinContext2.getChildContexts().add(inquiriesContext2);
                joinContext2.getChildContexts().add(hitRateContext2);

                ReportContext topContext = new ReportContext();
                topContext.setMultiContextMode("uniontranspose");
                topContext.getChildContexts().add(joinContext1);
                topContext.getChildContexts().add(joinContext2);

                Options options = new Options();
                options.setOffset(0);
                options.setLimit(10);

                subscribeReport(topContext, options, benchmarks, true, true);
            }
        }, 1);
    }

    @Test
    public void subscribeWithContextTest() throws Exception {
        benchmark(new IBenchmarkRunner() {
            @Override
            public void run(final Benchmarks benchmarks) throws Exception {

                String json = Resources.toString(Resources.getResource(EventLoopReactorTest.class, "ReportContext.json"), Charset.forName("utf-8"));
                ReportContext reportContext = new JacksonSerialiser().deserialise(json, ReportContext.class);

                Options options = new Options();
//                options.addSortColumn("client", false);
                options.setOffset(0);
                options.setLimit(100);

                subscribeReport(reportContext, options, benchmarks, false, false);
            }
        }, 1);
    }

    @Test
    public void subscribeWithContext2Test() throws Exception {
        String json = Resources.toString(Resources.getResource(EventLoopReactorTest.class, "ReportContext2.json"), Charset.forName("utf-8"));
        ReportContext reportContext = new JacksonSerialiser().deserialise(json, ReportContext.class);

        Options options = new Options();
        options.setOffset(0);
        options.setLimit(1000);

        subscribeReport(reportContext, options, null, true, false);
    }

    @Test
    public void testArrayFill() throws Exception {
        int[] ints = new int[1500000];
        long start = System.nanoTime();
        Arrays.fill(ints, -1);
        System.out.println(String.format("%3f", ((System.nanoTime() - start) / 1000000f)));
    }

    @Test
    public void subscribeCatalogTest() throws Exception {
        subscribeOperator("/", null, false, false, false);
    }

    @Test
    public void subscribeWithFilterTest() throws Exception {
        Options options = new Options();
        options.setLimit(10);
        options.setFilterMode(FilterOperator.FilterMode.Filter);
        options.setFilterExpression("name == \"report_registry\"");
        subscribeOperator("/", options, true, false, false);
    }

    @Test
    public void updateFilterTest() throws Exception {
        Options options = new Options();
        options.setLimit(10);
        options.setFilterMode(FilterOperator.FilterMode.Transparent);
        try (ClientSubscription clientSubscription = subscribeOperator("/", options, false, false, false)) {

            clientSubscription.update((opt) -> {
                opt.setFilterMode(FilterOperator.FilterMode.Filter);
                opt.setFilterExpression("name == \"report_registry\"");
            }).get();

            Thread.sleep(3000);
        }
    }

    @Test
    public void testReactorMonitor() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        Catalog catalog = new Catalog(executionContext);
        EventLoopReactor reactor = new EventLoopReactor("reactor", new Network(new CommandHandlerRegistry(), executionContext, catalog, null));
        executionContext.setReactor(reactor);
        reactor.start();

        final CountDownLatch latch = new CountDownLatch(1);
        reactor.scheduleTask(() -> {
            try {
                Thread.sleep(40000);
                latch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 0, -1);
        latch.await();
    }

    private ClientSubscription subscribeOperator(String name, Options options, boolean closeAfterSnapshot, boolean reset, boolean wait) throws Exception {
        TestViewServerClient client = new TestViewServerClient("client", new NettyTcpEndpoint("tcp://localhost:18000"));
        client.authenticate("open", "admin@viewserver.com");

        CountDownLatch snapshotLatch = new CountDownLatch(1);
        Future<ClientSubscription> subscription = client.subscribe(name, options, new SubscriptionEventHandlerBase<ClientSubscription>() {
            @Override
            public void onDataReset(ClientSubscription subscription) {
                log.info("Data reset!");
            }

            @Override
            public void onSnapshotComplete(ClientSubscription subscription) {
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
        });
        snapshotLatch.await();

        if (closeAfterSnapshot) {
            subscription.get().close();

            if (reset) {
                client.resetServer().get();
            }
            client.close();
        } else if (wait) {
            new CountDownLatch(1).await();
        }

        return subscription.get();
    }

    private void subscribeReport(ReportContext reportContext, Options options, Benchmarks benchmarks, boolean closeAfterShutdown, boolean reset) throws Exception {
        String clientName = benchmarks != null ? ("client_" + benchmarks.getRun()) : "client";
        TestViewServerClient client = new TestViewServerClient(clientName,
//                new NettyInProcEndpoint("master")
                new NettyTcpEndpoint("tcp://localhost:18000", true)
//                new NettyWebSocketEndpoint("wss://localhost:8080/", true)
//                new NettyIpcEndpoint("master.viewserver.ipc")
        );
        client.getConnectFuture().get();
        client.authenticate("open", "admin@viewserver.com");

        CountDownLatch snapshotLatch = new CountDownLatch(1);
        if (benchmarks != null) {
            benchmarks.startBenchmark("subscribing");
        }
        Future<ClientSubscription> subscription = client.subscribeToReport(reportContext, options,
                getSubscriptionEventHandler(benchmarks, snapshotLatch));
        snapshotLatch.await();

        if (closeAfterShutdown) {
            subscription.get().close();

            if (reset) {
                client.resetServer().get();
            }
            client.close();
        } else {
            new CountDownLatch(1).await();
        }
    }

    private void subscribeDimension(String dimension, ReportContext reportContext, Options options, Benchmarks benchmarks, boolean closeAfterShutdown, boolean reset) throws Exception {
        String clientName = benchmarks != null ? ("client_" + benchmarks.getRun()) : "client";
        TestViewServerClient client = new TestViewServerClient(clientName, new NettyTcpEndpoint("tcp://localhost:18000"));
        client.authenticate("open", "admin@viewserver.com");

        CountDownLatch snapshotLatch = new CountDownLatch(1);
        if (benchmarks != null) {
            benchmarks.startBenchmark("subscribing");
        }
        Future<ClientSubscription> subscription = client.subscribeToDimension(dimension, reportContext, options,
                getSubscriptionEventHandler(benchmarks, snapshotLatch));
        snapshotLatch.await();

        if (closeAfterShutdown) {
            subscription.get().close();

            if (reset) {
                client.resetServer().get();
            }
            client.close();
        } else {
            new CountDownLatch(1).await();
        }
    }

    private SubscriptionEventHandlerBase getSubscriptionEventHandler(final Benchmarks benchmarks, final CountDownLatch snapshotLatch) {
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
