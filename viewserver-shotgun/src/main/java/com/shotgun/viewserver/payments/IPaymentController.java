package com.shotgun.viewserver.payments;

import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.user.SavedPaymentCard;
import com.shotgun.viewserver.user.User;
import com.stripe.model.BankAccount;
import com.stripe.model.Card;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

import java.util.HashMap;
import java.util.List;

public interface IPaymentController {
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

    SavedPaymentCard addPaymentCard(PaymentCard paymentCard);

    void deletePaymentCard(String cardId);

    @ControllerAction(path = "getBankAccount", isSynchronous = false)
    BankAccount getBankAccount();

    @ControllerAction(path = "setBankAccount", isSynchronous = false)
    void setBankAccount(@ActionParam(name = "paymentBankAccount") PaymentBankAccount paymentBankAccount);
}

