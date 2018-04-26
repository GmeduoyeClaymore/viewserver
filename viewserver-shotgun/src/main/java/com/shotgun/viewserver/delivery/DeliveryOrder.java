package com.shotgun.viewserver.delivery;

import com.shotgun.viewserver.constants.OrderStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;





public class DeliveryOrder {
    public DeliveryAddress origin;
    public DeliveryAddress destination;

    public String customerId;
    public int estimatedCost;
    public String orderId;

    public ProductKey orderProduct;
    public Date requiredDate;
    public Date createdDate;

    public int noPeopleRequired;
    public int actualDistance;
    public int actualDuration;

    public List<OrderPaymentStage> payments;
    public List<DeliveryOrderFill> responses;
    public DeliveryOrderFill assignedPartner;
    public DeliveryOrderStatus status;
    public String description;

    public DeliveryOrder() {
    }

    public enum DeliveryOrderStatus {

        REQUESTED(OrderStatus.PLACED),
        RESPONDED(OrderStatus.PLACED),
        ASSIGNED(OrderStatus.ACCEPTED),
        ENROUTE(OrderStatus.INPROGRESS),
        PARTNERCOMPLETE(OrderStatus.INPROGRESS),
        CUSTOMERCOMPLETE(OrderStatus.COMPLETED);

        static{
            REQUESTED.to(RESPONDED, ASSIGNED);
            RESPONDED.to(RESPONDED, ASSIGNED);
            ASSIGNED.to(ENROUTE, RESPONDED);
            ENROUTE.to(PARTNERCOMPLETE);
            PARTNERCOMPLETE.to(CUSTOMERCOMPLETE);
        }

        private OrderStatus orderStatus;
        private DeliveryOrderStatus status;
        private List<DeliveryOrderStatus> permittedFrom = new ArrayList<>();
        private List<DeliveryOrderStatus> permittedTo = new ArrayList<>();

        public OrderStatus getOrderStatus() {
            return orderStatus;
        }

        public DeliveryOrderStatus getStatus() {
            return status;
        }

        public List<DeliveryOrderStatus> getPermittedFrom() {
            return permittedFrom;
        }

        public List<DeliveryOrderStatus> getPermittedTo() {
            return permittedTo;
        }

        private DeliveryOrderStatus from(DeliveryOrderStatus... statuses){
            for(DeliveryOrderStatus stat : statuses){
                if(!this.permittedFrom.contains(stat)){
                    this.permittedFrom.add(stat);
                }
            }
            return this;
        }

        private DeliveryOrderStatus to(DeliveryOrderStatus... statuses){
            for(DeliveryOrderStatus stat : statuses){
                if(!this.permittedTo.contains(stat)){
                    this.permittedTo.add(stat);
                }
                stat.from(this);
            }
            return this;
        }

        DeliveryOrderStatus(OrderStatus orderStatus) {
            this.orderStatus = orderStatus;
        }

        public DeliveryOrderStatus transitionTo(DeliveryOrderStatus status) {
            if(!this.equals(status) && !this.permittedTo.contains(status)){
                throw new RuntimeException("Cannot transition order from status " + this.name() + " to " + status.name());
            }
            return status;
        }

    }

    public static class OrderPaymentStage{
        public enum PaymentStageStatus{
            Pending,
            Complete,
            Paid
        }

        public OrderPaymentStage(PaymentStageStatus paymentStageStatus, String paymentId, int jobPercentage, Date lastUpdated) {
            this.paymentStageStatus = paymentStageStatus;
            this.paymentId = paymentId;
            this.jobPercentage = jobPercentage;
            this.lastUpdated = lastUpdated;
        }

        public PaymentStageStatus paymentStageStatus;
        public String paymentId;
        public int jobPercentage;
        public Date lastUpdated;
    }


    public static class DeliveryOrderFill{
        public String partnerId;
        public Date estimatedDate;
        public DeliveryOrderFillStatus fillStatus;
        public enum DeliveryOrderFillStatus{
            RESPONDED,
            DECLINED,
            ACCEPTED, CANCELLED,
        }

        public DeliveryOrderFill(String partnerId, Date estimatedDate, DeliveryOrderFillStatus fillStatus) {
            this.partnerId = partnerId;
            this.estimatedDate = estimatedDate;
            this.fillStatus = fillStatus;
        }
    }

}

