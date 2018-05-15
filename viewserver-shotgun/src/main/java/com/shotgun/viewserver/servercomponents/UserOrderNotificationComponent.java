package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.maps.LatLng;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.contracts.OrderNotificationContract;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import com.shotgun.viewserver.user.ProductSpreadFunction;
import com.shotgun.viewserver.user.User;
import com.shotgun.viewserver.user.UserRelationship;
import com.shotgun.viewserver.user.UserRelationshipStatus;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.DataSourceHelper;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.nodes.IndexOutputNode;
import io.viewserver.expression.function.Distance;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.index.IndexOperator;
import io.viewserver.operators.index.QueryHolderConfig;
import io.viewserver.operators.rx.EventType;
import io.viewserver.server.components.IBasicServerComponents;
import io.viewserver.server.components.IDataSourceServerComponents;
import io.viewserver.server.components.IServerComponent;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import io.viewserver.util.dynamic.NamedThreadFactory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscription;
import rx.schedulers.Schedulers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class UserOrderNotificationComponent implements IServerComponent, OrderNotificationContract {
    private IDataSourceServerComponents components;
    private IBasicServerComponents basicServerComponents;
    private static final Logger log = LoggerFactory.getLogger(UserOrderNotificationComponent.class);
    private List<Subscription> subscriptions = new ArrayList<>();
    private List<String> notifiedOrders = new ArrayList<>();
    private HashMap<String,Subscription> subscriptionsByUserId = new HashMap<>();
    private IMessagingController messagingController;
    private boolean disableDistanceCheck;
    private HashMap<String,List<String>> notifiedOrdersByUser;
    Executor notificationsExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("notifications"));

    public UserOrderNotificationComponent(IDataSourceServerComponents components, IBasicServerComponents basicServerComponents, ShotgunControllersComponents controllersComponents) {
        this.components = components;
        this.basicServerComponents = basicServerComponents;
        this.messagingController = controllersComponents.getMessagingController();
        notifiedOrdersByUser = new HashMap<>();
    }

    public UserOrderNotificationComponent disableDistanceCheck(){
        this.disableDistanceCheck = true;
        return this;
    }

    @Override
    public void start() {
        String operatorPath = IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, DataSource.TABLE_NAME);
        rx.Observable<IOperator> result = this.basicServerComponents.getServerCatalog().waitForOperatorAtThisPath(operatorPath);
        log.info("Waiting for user operator " + operatorPath);
        this.subscriptions.add(result.subscribe(operator -> {
            log.info("Found for user operator "  + operatorPath);
            listenForUsers(operator);
        }));
    }

    private void listenForUsers(IOperator operator) {

        IOutput out = operator.getOutput("out");
        this.subscriptions.add(out.observable("userId", "selectedContentTypes", "range", "latitude", "longitude", "relationships").
                filter(ev-> Arrays.asList(EventType.ROW_ADD,EventType.ROW_UPDATE).contains(ev.getEventType())).subscribe(ev -> {
            onUserAdded((HashMap)ev.getEventData());
        }));
        this.subscriptions.add(out.observable("userId").
                filter(ev-> Arrays.asList(EventType.ROW_REMOVE).contains(ev.getEventType())).subscribe(ev -> {
            onUserRemoved((HashMap)ev.getEventData());
        }));
    }

    private void onUserAdded(HashMap userMap) {
        User user = JSONBackedObjectFactory.create(userMap, User.class);
        log.info("Found for user - " + user.getUserId());
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
        this.components.onDataSourcesBuilt(OrderDataSource.NAME,UserDataSource.NAME).subscribe(c-> {
            rx.Observable<IOperator> result = this.basicServerComponents.getServerCatalog().waitForOperatorAtThisPath(IDataSourceRegistry.getOperatorPath(OrderDataSource.NAME, DataSource.INDEX_NAME));

            List<String> productIds  = ProductSpreadFunction.getProductIds(user.getSelectedContentTypes());
            if(productIds.size() == 0){
                log.info("No products found for user " + user.getUserId() + " no notifications will be sent");
                return;
            }
            log.info("Found for user - " + user.getUserId()  + " with products " + String.join(",",productIds));
            QueryHolderConfig[] queryHolders = DataSourceHelper.getQueryHolders(components.getDataSourceRegistry().get(OrderDataSource.NAME), Arrays.asList(getDimensionsForProductIds(productIds)),basicServerComponents.getExecutionContext().getDimensionMapper());
            AtomicReference<Subscription> subscribe1 = new AtomicReference<>();
            Subscription subscribe = result.subscribeOn(Schedulers.from(notificationsExecutor)).subscribe(operator -> {
                String nameForQueryHolders = IndexOutputNode.getNameForQueryHolders(Arrays.asList(queryHolders));
                log.info("Subscribing user - " + user.getUserId() + " to index output " + nameForQueryHolders);
                IOutput out = ((IndexOperator) operator).getOrCreateOutput(nameForQueryHolders, queryHolders);
                subscribe1.set(out.observable("orderId", "orderDetails").
                        filter(ev -> Arrays.asList(EventType.ROW_ADD, EventType.ROW_UPDATE).contains(ev.getEventType())).observeOn(Schedulers.from(notificationsExecutor)).subscribe(ev -> {
                    HashMap eventData = (HashMap) ev.getEventData();
                    BasicOrder order = JSONBackedObjectFactory.create((HashMap) eventData.get("orderDetails"), BasicOrder.class);
                    if(hasNotificationForUser(order.getOrderId(), user.getUserId())){//TODO fix this hack why does the index operator repeatedly fire row add events for the same order id
                        log.info("Already notified of " + order.getOrderId() + " not doing it again");
                        return;
                    }
                    if(order.getPartnerUserId() != null){
                        log.info("Partner already assigned not notifying the community of " + order.getOrderId());
                        return;
                    }
                    if(order.getCustomerUserId() == user.getUserId()){
                        log.info("This is my job ignoring ");
                        return;
                    }


                    log.info("Found order - " + order.getOrderId() + " for user " + user.getUserId() + " with products " + String.join(",", productIds));
                    NotificationStatus status = notifyUserOfNewOrder(order, user);
                    if( NotificationStatus.NotSentDontRetry.equals(status) ||  NotificationStatus.Sent.equals(status)){
                        setNotificationForUser(order.getOrderId(), user.getUserId());
                    }
                }, err -> log.error("Issue subscribing to orders {}", err)));

                this.subscriptions.add(subscribe1.get());
            }, err -> log.error("Issue subscribing to users {}", err));


            subscriptionsByUserId.put(user.getUserId(), new Subscription() {
                @Override
                public void unsubscribe() {
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

    private boolean hasNotificationForUser(String orderId, String userId) {
        List<String> notificationsForUser = this.notifiedOrdersByUser.get(userId);
        if(notificationsForUser == null){
            return false;
        }
        return notificationsForUser.contains(orderId);
    }

    private void setNotificationForUser(String orderId, String userId) {
        List<String> notificationsForUser = this.notifiedOrdersByUser.get(userId);
        if(notificationsForUser == null){
            notificationsForUser = new ArrayList<>();
            this.notifiedOrdersByUser.put(userId,notificationsForUser);
        }
        notificationsForUser.add(orderId);
    }

    DecimalFormat df = new DecimalFormat("#.00");

    private ReportContext.DimensionValue getDimensionsForProductIds(List<String> productIds) {
        return new ReportContext.DimensionValue("dimension_productId",false, productIds.toArray());
    }

    private NotificationStatus notifyUserOfNewOrder(BasicOrder order, User user) {
        if(new DateTime(order.getRequiredDate()).isBeforeNow()){
            log.info("Not resending historical order");
            return NotificationStatus.NotSentDontRetry;
        }
        if(user.isBlocked(order.getCustomerUserId())){
            log.info("Not sending as user " + user.getUserId() + " is blocked by " + order.getCustomerUserId());
            return NotificationStatus.NotSentRetry;
        }

        UserRelationship relationship = user.getRelationship(order.getPartnerUserId());
        Boolean justForFriends = order.isJustForFriends();
        if(justForFriends != null && justForFriends && (relationship == null || !UserRelationshipStatus.ACCEPTED.equals(relationship.getRelationshipStatus()))){
            log.info("Not sending as order is just for friends and  " + user.getUserId() + " is not a friend of " + order.getCustomerUserId());
            return  NotificationStatus.NotSentRetry;
        }

        LatLng userHome = user.getLocation();
        DeliveryAddress origin = order.getOrigin();
        double k1 = Distance.distance(userHome.getLatitude(), userHome.getLongitude(), origin.getLatitude(), origin.getLongitude(), "K");
        boolean k   = k1 <= user.getRange();
        if(k || disableDistanceCheck){
            sendMessage(order.getOrderId(), order.getCustomerUserId(),user.getUserId(), "Shotgun - New Job - £" + df.format(order.getAmount()/100), "A new job \"" + order.getTitle() + "\" has just been listed that may be of interest to you", false);
            return  NotificationStatus.Sent;
        }
        return  NotificationStatus.NotSentRetry;
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
