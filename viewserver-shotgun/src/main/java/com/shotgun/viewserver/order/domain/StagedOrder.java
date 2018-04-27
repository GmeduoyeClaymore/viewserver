package com.shotgun.viewserver.order.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface StagedOrder {
    public List<OrderPaymentStage> payments = new ArrayList<>();

    public static class OrderPaymentStage {
        public enum PaymentStageStatus {
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
}
