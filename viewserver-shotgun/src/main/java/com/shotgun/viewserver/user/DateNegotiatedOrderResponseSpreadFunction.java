package com.shotgun.viewserver.user;

import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.order.types.NegotiationResponse;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.ContentType;
import io.viewserver.operators.spread.ISpreadFunction;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateNegotiatedOrderResponseSpreadFunction implements ISpreadFunction {
    private static final Logger log = LoggerFactory.getLogger(DateNegotiatedOrderResponseSpreadFunction.class);
    private static final Column partnerId;
    private static final Column estimatedDate;
    private static final Column orderDetailWithoutResponses;
    private static final Column orderResponseStatus;


    public static String NAME = "getPartnerResponseIdsFromOrderDetail";
    public static String PARTNER_ID_COLUMN = "partnerId";
    public static String ESTIMATED_DATE_COLUMN = "estimatedDate";
    public static String ORDER_DETAIL_WITHOUT_RESPONSES = "orderDetailWithoutResponses";
    public static String PARTNER_ORDER_STATUS = "partnerOrderStatus";
    private static List<Column> columns = new ArrayList<>();

    static{
        partnerId = new Column(PARTNER_ID_COLUMN, ContentType.String);
        orderResponseStatus = new Column(PARTNER_ORDER_STATUS, ContentType.String);
        estimatedDate = new Column(ESTIMATED_DATE_COLUMN,ContentType.Date);
        orderDetailWithoutResponses = new Column(ORDER_DETAIL_WITHOUT_RESPONSES,ContentType.Json);
        columns.add(partnerId);
        columns.add(orderResponseStatus);
        columns.add(estimatedDate);
        columns.add(orderDetailWithoutResponses);

    }
    public DateNegotiatedOrderResponseSpreadFunction() {
    }

    @Override
    public List<Column> getColumns(){
        return this.columns;
    }

    @Override
    public List<Map.Entry<Column, Object[]>> getValues(int row, ColumnHolder columnHolder) {
        String contentTypeJSONString = (String) ColumnHolderUtils.getValue(columnHolder, row);
        if(contentTypeJSONString ==null || "".equals(contentTypeJSONString)){
            return new ArrayList<>();
        }
        NegotiatedOrder order = JSONBackedObjectFactory.create(contentTypeJSONString, NegotiatedOrder.class);
        NegotiationResponse[] responses = order.getResponses();
        if(responses == null){
            return new ArrayList<>();
        }
        Object[] customerIds = new Object[responses.length];
        Object[] customerResponseDates = new Object[responses.length];
        Object[] orderDetails = new Object[responses.length];
        Object[] statuses = new Object[responses.length];

        for(int i = 0; i< responses.length; i++){
            NegotiationResponse deliveryOrderFill = responses[i];
            customerIds[i] = deliveryOrderFill.getPartnerId();
            customerResponseDates[i] = deliveryOrderFill.getDate();
            orderDetails[i] = order.serialize("responses");
            statuses[i] = deliveryOrderFill.getResponseStatus().name();
        }


        List<HashMap.Entry<Column,Object[]>> result = new ArrayList<>();
        result.add(new HashMap.SimpleEntry(partnerId, customerIds));
        result.add(new HashMap.SimpleEntry(estimatedDate, customerResponseDates));
        result.add(new HashMap.SimpleEntry(orderDetailWithoutResponses, orderDetails));
        result.add(new HashMap.SimpleEntry(orderResponseStatus, statuses));
        return result;
    }

}
