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
    @ControllerAction(path = "createPaymentCustomer", isSynchronous = false)
    HashMap<String, Object> createPaymentCustomer(@ActionParam(name = "emailAddress") String emailAddress, @ActionParam(name = "paymentCard") PaymentCard paymentCard);

    @ControllerAction(path = "createPaymentAccount", isSynchronous = false)
    String createPaymentAccount(@ActionParam(name = "user") User user,
                                @ActionParam(name = "deliveryAddress") DeliveryAddress address,
                                @ActionParam(name = "paymentBankAccount") PaymentBankAccount paymentBankAccount);

    @ControllerAction(path = "createCharge", isSynchronous = false)
    void createCharge(@ActionParam(name = "totalPrice") Double totalPrice,
                      @ActionParam(name = "chargePercentage") int chargePercentage,
                      @ActionParam(name = "paymentId") String paymentId,
                      @ActionParam(name = "customerId") String customerId,
                      @ActionParam(name = "accountId") String accountId);

    @ControllerAction(path = "addPaymentCard", isSynchronous = false)
    String addPaymentCard(@ActionParam(name = "paymentCard") PaymentCard paymentCard);

    @ControllerAction(path = "getPaymentCards", isSynchronous = false)
    List<Card> getPaymentCards();

    @ControllerAction(path = "deletePaymentCard", isSynchronous = false)
    void deletePaymentCard(@ActionParam(name = "cardId") String cardId);

    @ControllerAction(path = "getBankAccount", isSynchronous = false)
    BankAccount getBankAccount();

    @ControllerAction(path = "setBankAccount", isSynchronous = false)
    void setBankAccount(@ActionParam(name = "paymentBankAccount") PaymentBankAccount paymentBankAccount);
}
