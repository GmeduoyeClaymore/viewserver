package com.shotgun.viewserver.user;

import com.shotgun.viewserver.delivery.DeliveryOrder;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.ContentType;
import io.viewserver.expression.function.IUserDefinedFunction;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionBool;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.operators.spread.ISpreadFunction;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryCustomerResponseSpreadFunction implements ISpreadFunction {
    private static final Logger log = LoggerFactory.getLogger(DeliveryCustomerResponseSpreadFunction.class);
    private final Column partnerId;
    private final Column estimatedDate;
    private final Column orderDetailWithoutResponses;
    private final Column orderResponseStatus;


    public static String NAME = "getPartnerResponseIdsFromOrderDetail";
    public static String PARTNER_ID_COLUMN = "partnerId";
    public static String ESTIMATED_DATE_COLUMN = "estimatedDate";
    public static String ORDER_DETAIL_WITHOUT_RESPONSES = "orderDetailWithoutResponses";
    public static String PARTNER_ORDER_STATUS = "partnerOrderStatus";

    public DeliveryCustomerResponseSpreadFunction() {
         partnerId = new Column(PARTNER_ID_COLUMN, ContentType.String);
         orderResponseStatus = new Column(PARTNER_ORDER_STATUS, ContentType.String);
         estimatedDate = new Column(ESTIMATED_DATE_COLUMN,ContentType.Date);
         orderDetailWithoutResponses = new Column(ORDER_DETAIL_WITHOUT_RESPONSES,ContentType.Json);
    }

    @Override
    public List<Map.Entry<Column, Object[]>> getValues(int row, ColumnHolder columnHolder) {
        String contentTypeJSONString = (String) ColumnHolderUtils.getValue(columnHolder, row);
        if(contentTypeJSONString ==null || "".equals(contentTypeJSONString)){
            return new ArrayList<>();
        }
        DeliveryOrder order = JacksonSerialiser.getInstance().deserialise(contentTypeJSONString, DeliveryOrder.class);
        List<DeliveryOrder.DeliveryOrderFill> responses = order.responses;
        if(responses == null){
            return new ArrayList<>();
        }
        Object[] customerIds = new Object[responses.size()];
        Object[] customerResponseDates = new Object[responses.size()];
        Object[] orderDetails = new Object[responses.size()];
        Object[] statuses = new Object[responses.size()];

        order.responses = null;

        for(int i = 0; i< responses.size(); i++){
            DeliveryOrder.DeliveryOrderFill deliveryOrderFill = responses.get(i);
            customerIds[i] = deliveryOrderFill.partnerId;
            customerResponseDates[i] = deliveryOrderFill.estimatedDate;
            orderDetails[i] = JacksonSerialiser.getInstance().serialise(order);
            statuses[i] = deliveryOrderFill.fillStatus.name();
        }


        List<HashMap.Entry<Column,Object[]>> result = new ArrayList<>();
        result.add(new HashMap.SimpleEntry(partnerId, customerIds));
        result.add(new HashMap.SimpleEntry(estimatedDate, customerResponseDates));
        result.add(new HashMap.SimpleEntry(orderDetailWithoutResponses, orderDetails));
        result.add(new HashMap.SimpleEntry(orderResponseStatus, statuses));
        return result;
    }

}
