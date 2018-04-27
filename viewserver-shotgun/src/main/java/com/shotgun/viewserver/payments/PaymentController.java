package com.shotgun.viewserver.payments;

import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.user.User;
import com.stripe.model.BankAccount;
import com.stripe.model.Card;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;

import java.util.HashMap;
import java.util.List;

public interface PaymentController {
    HashMap<String, Object> createPaymentCustomer(String emailAddress, PaymentCard paymentCard);

    String createPaymentAccount(User user, DeliveryAddress address, PaymentBankAccount paymentBankAccount);

    void createCharge(int totalPrice, int chargePercentage, String paymentId, String customerId, String accountId, String description);

    @ControllerAction(path = "addPaymentCard", isSynchronous = false)
    String addPaymentCard(@ActionParam(name = "paymentCard") PaymentCard paymentCard);

    @ControllerAction(path = "getPaymentCards", isSynchronous = false)
    List<Card> getPaymentCards();

    @ControllerAction(path = "deletePaymentCard", isSynchronous = false)
    void deletePaymentCard(@ActionParam(name = "cardId") String cardId);

    @ControllerAction(path = "getBankAccount", isSynchronous = false)
    BankAccount getBankAccount();

    void setBankAccount(PaymentBankAccount paymentBankAccount);
}
