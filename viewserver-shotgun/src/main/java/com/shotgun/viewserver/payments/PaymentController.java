package com.shotgun.viewserver.payments;

import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
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

    String createCharge(int totalPrice,
                        String paymentMethodId,
                        String fromCustomerUserId,
                        String toCustomerUserId,
                        String description);

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
