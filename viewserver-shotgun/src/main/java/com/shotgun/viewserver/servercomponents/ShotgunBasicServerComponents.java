package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.ContainsProduct;
import com.shotgun.viewserver.user.CategorySpreadFunction;
import com.shotgun.viewserver.user.ProductSpreadFunction;
import io.viewserver.network.IEndpoint;
import io.viewserver.operators.spread.SpreadFunctionRegistry;
import io.viewserver.server.components.BasicServerComponents;
import io.viewserver.server.components.NettyBasicServerComponent;

import java.util.List;

public class ShotgunBasicServerComponents extends NettyBasicServerComponent{

    public ShotgunBasicServerComponents(List<IEndpoint> endpointList) {
        super(endpointList);
    }

    @Override
    public void start() {
        super.start();
        this.getExecutionContext().getFunctionRegistry().register("containsProduct", ContainsProduct.class);
        SpreadFunctionRegistry spreadColumnRegistry = this.getExecutionContext().getSpreadColumnRegistry();
        spreadColumnRegistry.register("getProductIdsFromContentTypeJSON", ProductSpreadFunction.class);
        spreadColumnRegistry.register("getCategoryIdsFromContentTypeJSON", CategorySpreadFunction.class);
    }
}
