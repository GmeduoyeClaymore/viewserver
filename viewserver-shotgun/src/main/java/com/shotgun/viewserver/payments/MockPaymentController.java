package com.shotgun.viewserver.payments;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.user.User;
import com.stripe.model.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.network.IPeerSession;
import io.viewserver.network.netty.NettyChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Controller(name = "paymentController")
public class MockPaymentController implements PaymentController{
    private static final Logger logger = LoggerFactory.getLogger(MockPaymentController.class);

    public MockPaymentController() {
    }

    @ControllerAction(path = "createPaymentCustomer", isSynchronous = false)
    public HashMap<String, Object> createPaymentCustomer(@ActionParam(name = "emailAddress") String emailAddress, @ActionParam(name = "paymentCard") PaymentCard paymentCard) {
        String cardToken = createCardToken(paymentCard);
        Map<String, Object> customerParams = new HashMap<>();
        customerParams.put("email", emailAddress);
        customerParams.put("source", cardToken);

        try {
            Customer customer = createMockCustomer(customerParams);
            logger.debug("Added stripe payment customer with id {}", customer.getId());
            HashMap<String, Object> result = new HashMap<>();
            result.put("customerId", customer.getId());
            result.put("paymentToken", cardToken);
            return result;
        } catch (Exception e) {
            logger.error("There was a problem adding the payment customer", e);
            throw new RuntimeException(e);
        }
    }

    private Customer createMockCustomer(Map<String, Object> customerParams) {
        Customer customer = new Customer();
        UUID uuid = UUID.randomUUID();
        customer.setId(uuid.toString());
        customer.setEmail((String) customerParams.get("email"));
        return customer;
    }

    @ControllerAction(path = "createPaymentAccount", isSynchronous = false)
    public String createPaymentAccount(@ActionParam(name = "user") User user,
                                       @ActionParam(name = "deliveryAddress") DeliveryAddress address,
                                       @ActionParam(name = "paymentBankAccount") PaymentBankAccount paymentBankAccount) {
        Account account = null;
        Map<String, Object> accountParams = null;

        try {
            //https://stripe.com/docs/api#update_account
            Map<String, Object> payoutSchedule = new HashMap<>();
            payoutSchedule.put("delay_days", 7);
            payoutSchedule.put("interval", "weekly");
            payoutSchedule.put("weekly_anchor", "monday");

            Map<String, Object> tosAcceptance = new HashMap<>();
            tosAcceptance.put("date", (long) System.currentTimeMillis() / 1000L);
            IPeerSession session = ControllerContext.Current().getPeerSession();
            String ip = ((NioSocketChannel) ((NettyChannel) session.getChannel()).getChannel()).remoteAddress().getAddress().toString().substring(1);
            tosAcceptance.put("ip", ip);

            Map<String, Object> dob = new HashMap<>();
            Calendar c = Calendar.getInstance();
            if(user.getDob() == null){
                throw new RuntimeException("No DOB specified");
            }
            c.setTime(user.getDob());
            dob.put("day", c.get(Calendar.DAY_OF_MONTH));
            dob.put("month", c.get(Calendar.MONTH));
            dob.put("year", c.get(Calendar.YEAR));

            Map<String, Object> entityAddress = new HashMap<>();
            entityAddress.put("line1", address.getLine1());
            entityAddress.put("city", address.getCity());
            entityAddress.put("postal_code", address.getPostCode());

            Map<String, Object> legalEntity = new HashMap<>();
            legalEntity.put("address", entityAddress);
            legalEntity.put("dob", dob);
            legalEntity.put("first_name", user.getFirstName());
            legalEntity.put("last_name", user.getLastName());
            legalEntity.put("type", "individual");

            Map<String, Object> externalAccount = new HashMap<>();
            externalAccount.put("object", "bank_account");
            externalAccount.put("account_number", paymentBankAccount.getAccountNumber().replaceAll("[^\\d]", ""));
            externalAccount.put("routing_number", paymentBankAccount.getSortCode().replaceAll("[^\\d]", ""));
            externalAccount.put("account_holder_type", "individual");
            externalAccount.put("country", "GB");
            externalAccount.put("currency", "gbp");

            accountParams = new HashMap<>();
            accountParams.put("type", "custom");
            accountParams.put("country", "GB");
            accountParams.put("default_currency", "gbp");
            accountParams.put("email", user.getEmail());
            accountParams.put("external_account", externalAccount);
            accountParams.put("payout_schedule", payoutSchedule);
            accountParams.put("tos_acceptance", tosAcceptance);
            accountParams.put("legal_entity", legalEntity);

            account = Account.create(accountParams);
            logger.debug("Added stripe account id {} params are {}", account.getId(), ControllerUtils.toString(accountParams));
            return account.getId();
        } catch (Exception e) {
            logger.error(String.format("There was a problem creating the payment account \"%s\"", ControllerUtils.toString(accountParams)), e);
            throw new RuntimeException(e);
        }
    }

    @ControllerAction(path = "createCharge", isSynchronous = false)
    public void createCharge(@ActionParam(name = "totalPrice") Double totalPrice,
                             @ActionParam(name = "chargePercentage") int chargePercentage,
                             @ActionParam(name = "paymentId") String paymentId,
                             @ActionParam(name = "customerId") String customerId,
                             @ActionParam(name = "accountId") String accountId) {
        try {
            //TODO is there a better way to do this without using big decimals all over the place?
            BigDecimal chargeDecimal = BigDecimal.valueOf(chargePercentage).divide(BigDecimal.valueOf(100));
            BigDecimal destinationAmount = BigDecimal.valueOf(totalPrice.intValue()).subtract(BigDecimal.valueOf(totalPrice.intValue()).multiply(chargeDecimal).setScale(0, RoundingMode.DOWN));
            Map<String, Object> destinationParams = new HashMap<>();
            destinationParams.put("account", accountId);
            destinationParams.put("amount", destinationAmount);

            Map<String, Object> params = new HashMap<>();
            params.put("amount", totalPrice == null ? null : totalPrice.intValue());
            params.put("currency", "gbp");
            params.put("customer", customerId);
            params.put("source", paymentId);
            params.put("destination", destinationParams);
            Charge charge = Charge.create(params);
            logger.debug("Created stripe charge {} with amount {} with {} sent to driver", charge.getId(), totalPrice, destinationAmount);
        } catch (Exception e) {
            logger.error("There was a problem creating the charge", e);
            throw new RuntimeException(e);
        }
    }

    @ControllerAction(path = "addPaymentCard", isSynchronous = false)
    public String addPaymentCard(@ActionParam(name = "paymentCard") PaymentCard paymentCard) {
        try {
            String customerToken = getStripeCustomerToken();
            String cardToken = createCardToken(paymentCard);
            Customer customer = Customer.retrieve(customerToken);
            Map<String, Object> params = new HashMap<>();
            params.put("source", cardToken);
            customer.getSources().create(params);
            logger.debug("Added stripe payment card with token {}", cardToken);
            return cardToken;
        } catch (Exception e) {
            logger.error("There was a problem adding the payment card", e);
            throw new RuntimeException(e);
        }
    }

    @ControllerAction(path = "getPaymentCards", isSynchronous = false)
    public List<Card> getPaymentCards() {
        try {
            List<Card> result = new ArrayList<>();
            Card e = new Card();
            e.setExpMonth(12);
            e.setExpYear(22);
            e.setName("Mr OO Meduoye");
            e.setBrand("VISA");
            e.setLast4("4242");
            e.setIin("123");
            result.add(e);
            return result;
        } catch (Exception e) {
            logger.error("There was a problem getting the payment cards", e);
            throw new RuntimeException(e);
        }
    }

    private User getUser() {
        User user = (User) ControllerContext.get("user");
        if(user == null){
            throw new RuntimeException("User must be logged in to get current payment cards");
        }
        return user;
    }

    @ControllerAction(path = "deletePaymentCard", isSynchronous = false)
    public void deletePaymentCard(@ActionParam(name = "cardId") String cardId) {
        try {
            String customerToken = getStripeCustomerToken();
        } catch (Exception e) {
            logger.error("There was a problem deleting the payment card", e);
            throw new RuntimeException(e);
        }
    }

    @ControllerAction(path = "getBankAccount", isSynchronous = false)
    public BankAccount getBankAccount() {
        try {
            BankAccount bankAccount = new BankAccount();
            bankAccount.setAccount("XXXXXXX");
            return bankAccount;
        } catch (Exception e) {
            logger.error("There was a problem getting the bank account", e);
            throw new RuntimeException(e);
        }
    }

    private String getStripeAccountId() {
        User user = getUser();
        String stripeAccountId = user.getStripeAccountId();
        if(stripeAccountId == null){
            throw new RuntimeException("No stripe account id specified for current user");
        }
        return stripeAccountId;
    }

    private String getStripeCustomerToken() {
        User user = getUser();
        String stripeCustomerToken = user.getStripeCustomerId();
        if(stripeCustomerToken == null){
            throw new RuntimeException("No stripe customerid specified for current user");
        }
        return stripeCustomerToken;
    }

    @ControllerAction(path = "setBankAccount", isSynchronous = false)
    public void setBankAccount(@ActionParam(name = "paymentBankAccount") PaymentBankAccount paymentBankAccount) {
        try {
            String stripeAccountId = getStripeAccountId();
            Map<String, Object> externalAccount = new HashMap<>();
            externalAccount.put("object", "bank_account");
            externalAccount.put("account_number", paymentBankAccount.getAccountNumber().replaceAll("[^\\d]", ""));
            externalAccount.put("routing_number", paymentBankAccount.getSortCode().replaceAll("[^\\d]", ""));
            externalAccount.put("account_holder_type", "individual");
            externalAccount.put("country", "GB");
            externalAccount.put("currency", "gbp");

            Account account = Account.retrieve(stripeAccountId, null);

            Map<String, Object> accountParams = new HashMap<>();
            accountParams.put("external_account", externalAccount);
            account.update(accountParams);
            logger.debug("Set bank account for stripe account {}", stripeAccountId);
        } catch (Exception e) {
            logger.error("There was a problem setting the bank account", e);
            throw new RuntimeException(e);
        }
    }

    private String createCardToken(PaymentCard paymentCard) {

        Map<String, Object> tokenParams = new HashMap<>();
        Map<String, Object> cardParams = new HashMap<>();
        cardParams.put("number", paymentCard.getNumber());
        cardParams.put("exp_month", paymentCard.getExpMonth());
        cardParams.put("exp_year", paymentCard.getExpYear());
        cardParams.put("cvc", paymentCard.getCvc());
        tokenParams.put("card", cardParams);
        return tokenParams.toString();
    }
}