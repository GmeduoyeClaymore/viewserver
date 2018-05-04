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
    HashMap<String, Object> createPaymentCustomer(String emailAddress, PaymentCard paymentCard);

    HashMap<String, Object> createPaymentAccount(User user, DeliveryAddress address, PaymentBankAccount paymentBankAccount);

    String createCharge(int totalPrice, String paymentMethodId, String fromCustomerUserId, String toCustomerUserId, String description);

    SavedPaymentCard addPaymentCard(PaymentCard paymentCard);

    void deletePaymentCard(String cardId);

    void setBankAccount(PaymentBankAccount paymentBankAccount);
}

