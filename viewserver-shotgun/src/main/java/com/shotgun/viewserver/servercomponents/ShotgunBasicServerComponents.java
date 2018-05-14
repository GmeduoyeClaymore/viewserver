package com.shotgun.viewserver.servercomponents;

import com.fasterxml.jackson.databind.Module;
import com.shotgun.viewserver.ContainsProduct;
import com.shotgun.viewserver.user.*;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.network.IEndpoint;
import io.viewserver.operators.spread.SpreadFunctionRegistry;
import io.viewserver.server.components.NettyBasicServerComponent;

import java.util.List;

public class ShotgunBasicServerComponents extends NettyBasicServerComponent{

    private boolean disconnectOnTimeout;

    public ShotgunBasicServerComponents(List<IEndpoint> endpointList, boolean disconnectOnTimeout) {
        super(endpointList);
        this.disconnectOnTimeout = disconnectOnTimeout;
        JacksonSerialiser.getInstance().registerModules(
                new Module[]{
                        new OrderSerializationModule()
                }
        );
    }

    @Override
    public void start() {
        super.start();
        this.serverNetwork.setDisconnectOnTimeout(disconnectOnTimeout);
        this.getExecutionContext().getFunctionRegistry().register("containsProduct", ContainsProduct.class);
        this.getExecutionContext().getFunctionRegistry().register("getResponseField", GetPartnerResponseField.class);
        this.getExecutionContext().getFunctionRegistry().register("getOrderField", GetOrderField.class);
        this.getExecutionContext().getFunctionRegistry().register("getRelationship", GetRelationship.class);
        this.getExecutionContext().getFunctionRegistry().register("isBlocked", IsBlocked.class);
        this.getExecutionContext().getFunctionRegistry().register("isAfter", IsAfter.class);
        SpreadFunctionRegistry spreadColumnRegistry = this.getExecutionContext().getSpreadColumnRegistry();
        spreadColumnRegistry.register("getProductIdsFromContentTypeJSON", ProductSpreadFunction.class);
        spreadColumnRegistry.register("getCategoryIdsFromContentTypeJSON", CategorySpreadFunction.class);
        spreadColumnRegistry.register("getPartnerResponseIdsFromOrderDetail", DateNegotiatedOrderResponseSpreadFunction.class);
        spreadColumnRegistry.register(UserRelationshipsSpreadFunction.NAME, UserRelationshipsSpreadFunction.class);
    }
}


