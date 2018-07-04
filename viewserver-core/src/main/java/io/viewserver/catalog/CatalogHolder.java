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

import io.viewserver.Constants;
import io.viewserver.collections.IntHashSet;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.Utils;
import io.viewserver.operators.IOperator;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.viewserver.operators.IOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.ReplaySubject;

import java.util.*;

/**
 * Created by bemm on 14/10/15.
 */
public class CatalogHolder implements ICatalog {
    private final HashMap<String, IOperator> operatorsByName = new HashMap<>();
    private ReplaySubject<IOperator> operatorRegistrations = ReplaySubject.create();
    private ReplaySubject<ICatalog> childRegistrations = ReplaySubject.create();
    private final TIntObjectHashMap<IOperator> operatorsByHash = new TIntObjectHashMap<>();
    private final IntHashSet operatorIds = new IntHashSet(8, 0.75f, -1);
    private static final Logger log = LoggerFactory.getLogger(CatalogHolder.class);
    private Map<String, ICatalog> children = new HashMap<>();
    private IOperator owner;

    public CatalogHolder(IOperator owner) {
        this.owner = owner;
    }

    public IOperator getOperatorForRow(int row) {
        int hashCode = operatorIds.get(row);
        return operatorsByHash.get(hashCode);
    }

    public int getRowIdForOperator(IOperator operator) {
        int hashCode = operator.hashCode();
        return operatorIds.index(hashCode);
    }

    @Override
    public String getName() {
        return owner.getName();
    }

    @Override
    public IOutput getOutput() {
        return owner.getOutput(Constants.OUT);
    }

    @Override
    public ICatalog getParent() {
        return owner.getCatalog();
    }

    @Override
    public IExecutionContext getExecutionContext() {
        return owner.getExecutionContext();
    }

    @Override
    public int registerOperator(IOperator operator) {
        if (operatorsByName.putIfAbsent(operator.getName(), operator) != null) {
            throw new IllegalArgumentException("Operator '" + operator.getName() + "' already exists");
        }

        int hashCode = operator.hashCode();
        operatorsByHash.put(hashCode, operator);
        operatorIds.addInt(hashCode);

        if (operator instanceof ICatalog) {
            addChild((ICatalog)operator);
        }else{
            operatorRegistrations.onNext(operator);
        }

        return hashCode;
    }

    @Override
    public Observable<IOperator> waitForOperatorAtThisPath(String name) {
        int slash = name.indexOf('/');
        ICatalog catalog = this;
        if (slash == 0) {
            catalog = getRoot();
        }
        return getFromPath(catalog, Utils.splitIgnoringEmpty(name,"/"), 0);
    }



    private Observable<IOperator> getFromPath(ICatalog operator,String[] parts, int index){
        String part = parts[index];
        if(index == parts.length -1){
            return operator.waitForOperatorInThisCatalog(part);
        }
        return operator.waitForChild(part).flatMap(c-> getFromPath(c,parts, index+1));
    }

    @Override
    public IOperator getOperator(String name) {
        return operatorsByName.get(name);
    }

    @Override
    public IOperator getOperatorByPath(String name) {
        int slash = name.indexOf('/');
        ICatalog catalog = this;

        if (slash == 0) {
            catalog = getRoot();
        }
        String[] parts = Utils.splitIgnoringEmpty(name.trim(),"/");
        for(int i = 0;i < parts.length;i++){
            String part = parts[i];
            if("".equals(part)){
                continue;
            }
            if(i == parts.length -1){
                IOperator operator = catalog.getOperator(part);
                if(operator == null){
                    log.trace(String.format("Unable to find operator named \"%s\" in catalog. Full path  \"%s\"",part,name));
                    return null;
                }
                return operator;
            }else{
                catalog = catalog.getChild(part);
                if(catalog == null){
                    log.trace(String.format("Unable to find catalog named \"%s\" in catalog full path  \"%s\"",part,name));
                    return null;
                }
            }
        }

        log.warn(String.format("Unable to find operator named \"%s\" in catalog \"%s\"",name,catalog.getName()));
        return null;
    }


    private ICatalog getRoot() {
        ICatalog result = this;
        do{
            if(result.getParent() == null) {
                return  result;
            }
            result = result.getParent();
        }while (true);
    }


    @Override
    public Observable<IOperator> waitForOperatorInThisCatalog(String name) {
        if(operatorsByName.containsKey(name)){
            return rx.Observable.just(operatorsByName.get(name));
        }
        return operatorRegistrations.filter(op -> op.getName().equals(name)).take(1);
    }

    @Override
    public Observable<ICatalog> waitForChild(String name) {
        if(children.containsKey(name)){
            return rx.Observable.just(children.get(name));
        }
        return childRegistrations.filter(op -> op.getName().equals(name)).take(1);
    }


    @Override
    public java.util.Collection<IOperator> getAllOperators() {
        return Collections.unmodifiableCollection(operatorsByName.values());
    }

    @Override
    public void unregisterOperator(IOperator operator) {
        if (operatorsByName.remove(operator.getName()) == null) {
            throw new IllegalArgumentException("Operator '" + operator.getName() + "' does not exist");
        }
        int hashCode = operator.hashCode();
        operatorsByHash.remove(hashCode);
        operatorIds.remove(hashCode);

        if (operator instanceof ICatalog) {
            removeChild((ICatalog) operator);
        }
    }

    @Override
    public ICatalog createDescendant(String path) {
        if (path.startsWith("/") && getParent() != null) {
            throw new IllegalArgumentException("Descendant paths cannot begin with '/' except in the root catalog.");
        }

        ICatalog tempParent = this;
        String[] components = Utils.splitIgnoringEmpty(path,"/");
        boolean created = false;
        for (String component : components) {
            ICatalog existingCatalog = getChild(component);
            if (existingCatalog == null) {
                tempParent = new Catalog(component, tempParent);
                created = true;
            } else {
                tempParent = existingCatalog;
            }
        }
        if (!created) {
            throw new IllegalArgumentException("A descendant already exists at " + path);
        }
        return tempParent;
    }

    @Override
    public void tearDown() {
        for (ICatalog child : children.values()) {
            child.tearDown();
        }
        children.clear();

        if (getParent() != null) {
            getParent().removeChild(this);
        }

        ArrayList<IOperator> operators = new ArrayList<>(this.operatorsByName.values());
        for (IOperator operator : operators) {
            operator.tearDown();
        }
    }

    @Override
    public void addChild(ICatalog childCatalog) {
        if (children.putIfAbsent(childCatalog.getName(), childCatalog) != null) {
            throw new IllegalArgumentException(String.format("Catalog '%s' already has a child '%s'",
                    owner.getPath(), childCatalog.getName()));
        }
        if(childRegistrations.hasObservers()) {
            childRegistrations.onNext(childCatalog);
        }
    }

    @Override
    public void removeChild(ICatalog childCatalog) {
        children.remove(childCatalog.getName());
    }

    @Override
    public ICatalog getChild(String name) {
        Optional<ICatalog> child = children.values().stream().filter((c) -> c.getName().equals(name)).findFirst();
        return child.isPresent() ? child.get() : null;
    }

    @Override
    public ICatalog getDescendant(String path) {
        ICatalog tempParent = this;
        String[] components = Utils.splitIgnoringEmpty(path,"/");
        for (String component : components) {
            if("".equals(component)){
                continue;
            }
            tempParent = getChild(component);
        }
        return tempParent;
    }
}
