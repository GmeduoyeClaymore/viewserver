package com.shotgun.viewserver.delivery;

public class PaymentStage{
    public enum State{
        Started,
        Completed,
        Paid
    }
    private String stageName;
    private int stagePercentage;
}
