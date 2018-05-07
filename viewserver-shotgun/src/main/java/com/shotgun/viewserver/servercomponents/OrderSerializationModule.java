package com.shotgun.viewserver.servercomponents;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.shotgun.viewserver.delivery.Dimensions;
import com.shotgun.viewserver.delivery.ProductKey;
import com.shotgun.viewserver.delivery.Vehicle;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.maps.DistanceAndDuration;
import com.shotgun.viewserver.order.domain.LinkedDeliveryOrder;
import com.shotgun.viewserver.order.domain.OrderPaymentStage;
import com.shotgun.viewserver.order.domain.OrderProduct;
import com.shotgun.viewserver.order.types.NegotiationResponse;
import com.shotgun.viewserver.order.types.OrderContentType;
import com.shotgun.viewserver.user.SavedBankAccount;
import com.shotgun.viewserver.user.SavedPaymentCard;
import com.shotgun.viewserver.user.UserRating;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Date;

public class OrderSerializationModule extends SimpleModule {
    public OrderSerializationModule() {
        addSerializer(new ProductKeySerialiser());
        addSerializer(new OrderContentTypeSerializer());
        addSerializer(new DateSerialiser());
        registerDynamicClass(NegotiationResponse.class);
        registerDynamicClass(OrderPaymentStage.class);
        registerDynamicClass(LinkedDeliveryOrder.class);
        registerDynamicClass(OrderProduct.class);
        registerDynamicClass(UserRating.class);
        registerDynamicClass(SavedPaymentCard.class);
        registerDynamicClass(SavedBankAccount.class);
        registerDynamicClass(DeliveryAddress.class);
        registerDynamicClass(Dimensions.class);
        registerDynamicClass(DistanceAndDuration.class);
        registerDynamicClass(Vehicle.class);
        addDeserializer(ProductKey.class, new ProductKeyDesSerialiser());
        addDeserializer(Date.class, new DateDesSerialiser());

    }

    public <T> void  registerDynamicClass(Class<T> dynamicClass){
        addSerializer(new DynamicJsonBackedObjectSerialiser(dynamicClass));
        addDeserializer(dynamicClass, new DynamicJsonBackedObjectDesSerialiser(dynamicClass));
    }

    public static class ProductKeySerialiser extends StdSerializer<ProductKey> {
        public ProductKeySerialiser() {
            super(ProductKey.class);
        }

        @Override
        public void serialize(ProductKey value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeString(value.toString());
        }
    }
    public static class OrderContentTypeSerializer extends StdSerializer<OrderContentType> {
        public OrderContentTypeSerializer() {
            super(OrderContentType.class);
        }

        @Override
        public void serialize(OrderContentType value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeNumber(value.getContentTypeId());
        }
    }

    public static class ProductKeyDesSerialiser extends StdDeserializer<ProductKey> {
        public ProductKeyDesSerialiser() {
            super(ProductKey.class);
        }

        @Override
        public ProductKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final ObjectMapper mapper = (ObjectMapper) p.getCodec();
            final String node = mapper.readValue(p, String.class);
            return new ProductKey(node);
        }
    }
    public static class DateDesSerialiser extends StdDeserializer<Date> {
        public DateDesSerialiser() {
            super(Date.class);
        }

        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final ObjectMapper mapper = (ObjectMapper) p.getCodec();
            final String node = mapper.readValue(p, String.class);
            if(node == null || "".equals(node)){
                return null;
            }
            if(StringUtils.isNumeric(node)){
                return new Date(Long.parseLong(node));
            }
            return new DateTime(node).toDate();
        }
    }

    public static class DateSerialiser extends StdSerializer<Date> {
        public DateSerialiser() {
            super(Date.class);
        }

        @Override
        public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeString(new DateTime(value).toString());
        }
    }

    public static class DynamicJsonBackedObjectDesSerialiser<TDynamicObject extends DynamicJsonBackedObject> extends StdDeserializer<TDynamicObject> {
        private Class<TDynamicObject> dynamicObjectClass;

        public DynamicJsonBackedObjectDesSerialiser(Class<TDynamicObject> dynamicObjectClass) {
            super(dynamicObjectClass);
            this.dynamicObjectClass = dynamicObjectClass;
        }

        @Override
        public TDynamicObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final ObjectMapper mapper = (ObjectMapper) p.getCodec();
            JsonNode node = mapper.readTree(p);
            String orderDetailsString = node.toString();
            return JSONBackedObjectFactory.create(orderDetailsString,dynamicObjectClass);
        }
    }

    public static class DynamicJsonBackedObjectSerialiser<TDynamicObject extends DynamicJsonBackedObject> extends StdSerializer<TDynamicObject> {
        public DynamicJsonBackedObjectSerialiser(Class<TDynamicObject> dynamicObjectClass) {
            super(dynamicObjectClass);
        }

        @Override
        public void serialize(TDynamicObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeObject(value.getFields());
        }
    }
}
