package com.shotgun.viewserver.order.controllers.contracts;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.order.contracts.PaymentNotifications;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.order.domain.SinglePaymentOrder;
import com.shotgun.viewserver.payments.IPaymentController;
import com.shotgun.viewserver.user.User;
import com.shotgun.viewserver.user.UserRating;
import com.shotgun.viewserver.user.UserTransformationController;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;

import java.util.concurrent.atomic.AtomicReference;

import static com.shotgun.viewserver.ControllerUtils.getUserId;

public interface SinglePaymentOrderController extends OrderTransformationController, PaymentNotifications {






}

