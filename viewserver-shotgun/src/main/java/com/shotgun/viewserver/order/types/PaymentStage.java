package com.shotgun.viewserver.order.types;

public class PaymentStage{
    public enum State{
        Started,
        Completed,
        Paid
    }
    private String stageName;
    private int stagePercentage;
}
