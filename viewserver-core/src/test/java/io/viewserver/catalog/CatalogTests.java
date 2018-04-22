/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.catalog;

import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.filter.FilterOperator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by nick on 04/03/2015.
 */
public class CatalogTests {
    @Test
    public void canGetOperatorsTest() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        Catalog rootCatalog = new Catalog(executionContext);
        FilterOperator filter1 = new FilterOperator("filter1", executionContext, rootCatalog, null);

        Catalog child1 = new Catalog("child1", rootCatalog);
        FilterOperator child1filter1 = new FilterOperator("filter1", executionContext, child1, null);

        Catalog grandchild1 = new Catalog("grandchild1", child1);
        FilterOperator filter2 = new FilterOperator("filter2", executionContext, grandchild1, null);

        Catalog child2 = new Catalog("child2", rootCatalog);
        FilterOperator filter3 = new FilterOperator("filter3", executionContext, child2, null);

        Assert.assertEquals("/filter1", filter1.getPath());
        Assert.assertEquals("/child1", child1.getPath());
        Assert.assertEquals("/child1/filter1", child1filter1.getPath());
        Assert.assertEquals("/child1/grandchild1", grandchild1.getPath());
        Assert.assertEquals("/child1/grandchild1/filter2", filter2.getPath());
        Assert.assertEquals("/child2", child2.getPath());
        Assert.assertEquals("/child2/filter3", filter3.getPath());

        // get immediate children by relative path
        Assert.assertEquals(filter1, rootCatalog.getOperatorByPath("filter1"));
        Assert.assertEquals(child1, rootCatalog.getOperatorByPath("child1"));

        // get further descendants by relative path
        Assert.assertEquals(filter2, rootCatalog.getOperatorByPath("child1/grandchild1/filter2"));
        Assert.assertEquals(filter2, child1.getOperatorByPath("grandchild1/filter2"));

        // get own children before going up the tree
        Assert.assertEquals(child1filter1, child1.getOperatorByPath("filter1"));

        // can go up the tree implicitly
        Assert.assertEquals(child1filter1, grandchild1.getOperatorByPath("filter1"));

        // can go up the tree explicitly
        Assert.assertEquals(filter1, child1.getOperatorByPath("../filter1"));
        Assert.assertEquals(filter3, child1.getOperatorByPath("../child2/filter3"));
        Assert.assertEquals(filter1, child1.getOperatorByPath("../child2/../filter1"));

        // get root catalog from anywhere
        Assert.assertEquals(rootCatalog, rootCatalog.getOperatorByPath("/"));
        Assert.assertEquals(rootCatalog, child1.getOperatorByPath("/"));
        Assert.assertEquals(rootCatalog, grandchild1.getOperatorByPath("/"));
        Assert.assertEquals(rootCatalog, child2.getOperatorByPath("/"));

        // get by absolute path from anywhere
        Assert.assertEquals(child1filter1, rootCatalog.getOperatorByPath("/child1/filter1"));
        Assert.assertEquals(child1filter1, child1.getOperatorByPath("/child1/filter1"));
        Assert.assertEquals(child1filter1, grandchild1.getOperatorByPath("/child1/filter1"));
        Assert.assertEquals(child1filter1, child2.getOperatorByPath("/child1/filter1"));
    }
}
