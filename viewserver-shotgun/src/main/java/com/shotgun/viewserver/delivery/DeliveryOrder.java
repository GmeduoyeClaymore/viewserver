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
    public int noPeopleRequired;

    public ProductKey requiredVehicleType;
    public Date requiredDate;
    public Date createdDate;
    public int actualDistance;
    public int actualDuration;
    public List<DeliveryOrderFill> responses;
    public DeliveryOrderFill assignedPartner;
    public DeliveryOrderStatus status;


    public enum DeliveryOrderStatus {

        REQUESTED(OrderStatus.PLACED),
        RESPONDED(OrderStatus.PLACED),
        ASSIGNED(OrderStatus.ACCEPTED),
        ENROUTE(OrderStatus.INPROGRESS),
        PARTNERCOMPLETE(OrderStatus.INPROGRESS),
        CUSTOMERCOMPLETE(OrderStatus.INPROGRESS);

        static{
            REQUESTED.to(RESPONDED, ASSIGNED);
            RESPONDED.to(RESPONDED, ASSIGNED);
            ASSIGNED.to(ENROUTE);
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
    }

    public class DeliveryOrderFill{
        public String partnerId;
        public Date estimatedDate;
    }

}

