package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.contracts.OrderNotificationContract;
import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import com.shotgun.viewserver.setup.datasource.OrderWithResponseDataSource;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import com.shotgun.viewserver.user.ProductSpreadFunction;
import com.shotgun.viewserver.user.User;
import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.ObservableCommandResult;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.Options;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.SystemReportExecutor;
import io.viewserver.execution.context.ReportContextExecutionPlanContext;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.filter.FilterOperator;
import io.viewserver.operators.rx.EventType;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.report.ReportContextRegistry;
import io.viewserver.report.ReportDefinition;
import io.viewserver.report.ReportRegistry;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.server.components.IBasicServerComponents;
import io.viewserver.server.components.IDataSourceServerComponents;
import io.viewserver.server.components.IServerComponent;
import io.viewserver.server.components.ReportServerComponents;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import io.viewserver.util.dynamic.NamedThreadFactory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.viewserver.command.SubscribeReportHandler.enhance;
import static io.viewserver.core.Utils.toArray;

public class UserOrderNotificationComponent implements IServerComponent, OrderNotificationContract {
    private final DateTime startTime;
    private IDataSourceServerComponents components;
    private IBasicServerComponents basicServerComponents;
    private static final Logger log = LoggerFactory.getLogger(UserOrderNotificationComponent.class);
    private List<Subscription> subscriptions = new ArrayList<>();
    private HashMap<String,Subscription> subscriptionsByUserId = new HashMap<>();
    private IMessagingController messagingController;
    private ClientVersionInfo clientVersionInfo;
    private SystemReportExecutor systemReportExecutor;
    private HashMap<String,List<String>> notifiedOrdersByUser;
    Executor notificationsExecutor = Executors.newFixedThreadPool(1,new NamedThreadFactory("notifications"));
    private ReportContextRegistry reportContextRegistry;
    private ReportRegistry reportRegistry;

    public UserOrderNotificationComponent(IDataSourceServerComponents components, IBasicServerComponents basicServerComponents, ShotgunControllersComponents controllersComponents, ReportServerComponents reportServerComponents, ClientVersionInfo clientVersionInfo) {
        this.components = components;
        this.basicServerComponents = basicServerComponents;
        this.messagingController = controllersComponents.getMessagingController();
        this.clientVersionInfo = clientVersionInfo;

        this.startTime = new DateTime();

        notifiedOrdersByUser = new HashMap<>();
        this.systemReportExecutor = reportServerComponents.getSystemReportExecutor();
        this.reportContextRegistry = reportServerComponents.getReportContextRegistry();
        this.reportRegistry = reportServerComponents.getReportRegistry();
    }


    @Override
    public Observable start() {
        String operatorPath = IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, DataSource.TABLE_NAME);
        rx.Observable<IOperator> result = this.basicServerComponents.getServerCatalog().waitForOperatorAtThisPath(operatorPath);
        log.debug("Waiting for user operator " + operatorPath);
        this.subscriptions.add(result.subscribe(operator -> {
            log.debug("Found for user operator "  + operatorPath);
            listenForUsers(operator);
        }));
        return null;
    }

    private void listenForUsers(IOperator operator) {

        IOutput out = operator.getOutput("out");
        this.subscriptions.add(out.observable("userId", "selectedContentTypes", "relationships").
                filter(ev-> Arrays.asList(EventType.ROW_ADD).contains(ev.getEventType())).subscribe(ev -> {
            onUserAdded((HashMap)ev.getEventData());
        }));
        this.subscriptions.add(out.observable("userId", "selectedContentTypes", "relationships").
                filter(ev-> Arrays.asList(EventType.ROW_UPDATE).contains(ev.getEventType())).subscribe(ev -> {

            HashMap eventData = (HashMap) ev.getEventData();
            if(eventData.containsKey("selectedContentTypes")){
                log.debug("Resubscribing user as content types have changed");
                onUserAdded(eventData);
            }else{
                log.debug("Not resubscribing user as content types have not changed");
            }
        }));
        this.subscriptions.add(out.observable("userId").
                filter(ev-> Arrays.asList(EventType.ROW_REMOVE).contains(ev.getEventType())).subscribe(ev -> {
            onUserRemoved((HashMap)ev.getEventData());
        }));
    }

    private void onUserAdded(HashMap userMap) {
        User user = JSONBackedObjectFactory.create(userMap, User.class);
        log.debug("Found for user - " + user.getUserId());
        listenForUserProducts(user);
    }
    private void onUserRemoved(HashMap userMap) {
        User user = JSONBackedObjectFactory.create(userMap, User.class);
        if(subscriptionsByUserId.containsKey(user.getUserId())){
            subscriptionsByUserId.get(user.getUserId()).unsubscribe();
        }
    }

    private void listenForUserProducts(User user) {
        if(subscriptionsByUserId.containsKey(user.getUserId())){
            subscriptionsByUserId.get(user.getUserId()).unsubscribe();
        }
        this.components.onDataSourcesBuilt(OrderDataSource.NAME,UserDataSource.NAME, OrderWithResponseDataSource.NAME).subscribe(c-> {
            rx.Observable<IOperator> result = this.basicServerComponents.getServerCatalog().waitForOperatorAtThisPath(IDataSourceRegistry.getOperatorPath(OrderDataSource.NAME, DataSource.INDEX_NAME));

            if(user.getSelectedContentTypes() == null){
                log.info("No products found for user " + user.getUserId() + " no notifications will be sent");
                return;
            }
            AtomicReference<Subscription> subscribe1 = new AtomicReference<>();
            Subscription subscribe = result.subscribe(operator -> {
                getOrCreateOutput(user).subscribe(
                        out -> {
                            log.debug(user.getUserId() + " is susbscribing to operator " + out.getOwner().getName() + " at path " + out.getOwner().getPath());
                            subscribe1.set(out.observable("orderId", "orderDetails").observeOn(Schedulers.from(notificationsExecutor)).subscribe(ev -> {
                                log.debug("Received data for - " + user.getUserId() + " event is " + ev.getEventType());
                                if(Arrays.asList(EventType.ROW_ADD, EventType.ROW_UPDATE).contains(ev.getEventType())){
                                    HashMap eventData = (HashMap) ev.getEventData();
                                    String  orderId = (String) eventData.get("orderId");
                                    DateTime lastModified = new DateTime(eventData.get("lastModified"));
                                    HashMap orderDetails = (HashMap) eventData.get("orderDetails");
                                    log.debug("Received data for - " + user.getUserId() + " orderId  is " + orderId);

                                    if(!this.isMaster()){
                                        log.info("Not sending notification as I am not the master");
                                        return;
                                    }

                                    if(setNotificationForUser(orderId, lastModified, user.getUserId())){
                                        log.info("Sending notification to user {}",user.getUserId());
                                        notifyUserOfNewOrder(orderId,orderDetails, user);
                                    }
                                    else{
                                         log.info("Not sending notification to user {} as already sent",user.getUserId());
                                    }
                                }

                            }, err -> log.error("Issue subscribing to orders {}", err)));

                            log.debug("Adding subscription for - " + user.getUserId());
                            this.subscriptions.add(subscribe1.get());
                        },
                        err -> log.error("Issue subscribing to report output",err)
                );

            }, err -> log.error("Issue subscribing to users {}", err));


            subscriptionsByUserId.put(user.getUserId(), new Subscription() {
                @Override
                public void unsubscribe() {
                    log.debug("Unsubscribing " + user.getUserId());
                    if(subscribe1.get() != null){
                        subscribe1.get().unsubscribe();
                    }
                    if(subscribe != null){
                        subscribe.unsubscribe();
                    }
                }

                @Override
                public boolean isUnsubscribed() {
                    return false;
                }
            });});
    }

    private boolean isMaster() {
        KeyedTable table = (KeyedTable) basicServerComponents.getServerCatalog().getOperatorByPath(TableNames.CLUSTER_TABLE_NAME);
        if(table == null){
            log.info("Cannot determine if I am master as cannot find the load balancer table");
        }
        Boolean result = (Boolean) ColumnHolderUtils.getColumnValue(table,"isMaster",clientVersionInfo.getServerEndPoint());
        if(result == null){
            return false;
        }
        return result;
    }

    private rx.Observable<IOutput> getOrCreateOutput(User user) {

        ReportContext reportContext = createContext(user);
        ReportDefinition definition = reportRegistry.getReportById(reportContext.getReportName());
        enhance(definition,reportContext);

        log.debug("Subscribe command for context: {}", reportContext);

        ObservableCommandResult systemExecutionPlanResult = new ObservableCommandResult();

        ICatalog catalog = reportContextRegistry.getOrCreateCatalogForContext(reportContext);

        return Observable.<IOutput>create(iOutputEmitter -> {

            ReportContextExecutionPlanContext activeExecutionPlanContext = systemReportExecutor.executeContext(reportContext,
                    basicServerComponents.getExecutionContext(),
                    catalog,
                    systemExecutionPlanResult);

            systemExecutionPlanResult.observable().filter(c-> c.isSuccess()).take(1).timeout(4, TimeUnit.SECONDS , Observable.error(new RuntimeException("Unable to detect successful execcution plan subscription after 4 seconds"))).subscribe(c -> {
                IOperator operatorByPath = catalog.getOperatorByPath(activeExecutionPlanContext.getInputOperator());
                iOutputEmitter.onNext(operatorByPath.getOutput(Constants.OUT));
            });

        }, Emitter.BackpressureMode.BUFFER).subscribeOn(Schedulers.from(basicServerComponents.getExecutionContext().getReactor().getExecutor()));

    }

    private ReportContext createContext(User user) {
        //| dimension_customerUserId | String  | @userId | exclude  |
        //| dimension_status         | String  | PLACED  |          |
        //| showOutOfRange           | String  | true    |          |
        //| partnerLatitude          | Integer | 0       |          |
        //| showUnrelated            | String  | true    |          |
        //| maxDistance              | Integer | 0       |          |
        //| partnerLongitude         | Integer | 0       |          |
        ReportContext context = new ReportContext();
        context.setReportName("orderRequest");
        List<String> productIds  = ProductSpreadFunction.getProductIds(user.getSelectedContentTypes());
        List<ReportContext.DimensionValue> dimensionValues = context.getDimensionValues();
        dimensionValues.add(new ReportContext.DimensionValue("dimension_customerUserId", true,user.getUserId()));
        dimensionValues.add(new ReportContext.DimensionValue("dimension_status", false,"PLACED"));
        dimensionValues.add(new ReportContext.DimensionValue("dimension_productId", false,(Object[])toArray(productIds,String[]::new)));
        Map<String, ValueLists.IValueList> parameterValues = context.getParameterValues();
        parameterValues.put("showOutOfRange", ValueLists.valueListOf("true"));
        parameterValues.put("@userId", ValueLists.valueListOf(user.getUserId()));
        parameterValues.put("partnerLatitude", ValueLists.valueListOf(0));
        parameterValues.put("partnerLongitude", ValueLists.valueListOf(0));
        parameterValues.put("showUnrelated", ValueLists.valueListOf("true"));
        parameterValues.put("maxDistance", ValueLists.valueListOf(0));
        return context;
    }


    private synchronized boolean setNotificationForUser(String orderId, DateTime lastModified, String userId) {
        List<String> notificationsForUser = this.notifiedOrdersByUser.get(userId);
        if(notificationsForUser == null){
            notificationsForUser = new ArrayList<>();
            this.notifiedOrdersByUser.put(userId,notificationsForUser);
        }
        if(!notificationsForUser.contains(orderId) && lastModified.isAfter(startTime)){
            notificationsForUser.add(orderId);
            return true;
        }
        return false;
    }

    DecimalFormat df = new DecimalFormat("#.00");

    private ReportContext.DimensionValue getDimensionsForProductIds(List<String> productIds) {
        return new ReportContext.DimensionValue("dimension_productId",false, productIds.toArray());
    }

    private synchronized void notifyUserOfNewOrder(String orderId, HashMap order, User user) {
        String customerUserId = (String) order.get("customerUserId");
        String amount = df.format(Integer.parseInt(order.get("amount") + "") / 100);
        Object title = order.get("title");
        sendMessage(orderId, customerUserId, user.getUserId(), "Shotgun - New Job - £" + amount, "A new job \"" + title + "\" has just been listed that may be of interest to you", false);
    }

    @Override
    public void stop() {
        new ArrayList<>(this.subscriptions).forEach(c-> c.unsubscribe());
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public IMessagingController getMessagingController() {
        return messagingController;
    }

    enum NotificationStatus{
        NotSentRetry,
        NotSentDontRetry,
        Sent
    }
}
