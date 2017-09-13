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

import io.viewserver.collections.IntHashSet;
import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.IOperator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.*;

/**
 * Created by nick on 14/10/15.
 */
public class CatalogHolder implements ICatalog {
    private final HashMap<String, IOperator> operatorsByName = new HashMap<>();
    private final TIntObjectHashMap<IOperator> operatorsByHash = new TIntObjectHashMap<>();
    private final IntHashSet operatorIds = new IntHashSet(8, 0.75f, -1);
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
    public ICatalog getParent() {
        return owner.getCatalog();
    }

    @Override
    public ExecutionContext getExecutionContext() {
        return owner.getExecutionContext();
    }

    @Override
    public void registerOperator(IOperator operator) {
        if (operatorsByName.putIfAbsent(operator.getName(), operator) != null) {
            throw new IllegalArgumentException("Operator '" + operator.getName() + "' already exists");
        }
        int hashCode = operator.hashCode();
        operatorsByHash.put(hashCode, operator);
        operatorIds.addInt(hashCode);

        if (operator instanceof ICatalog) {
            addChild((ICatalog)operator);
        }
    }

    @Override
    public IOperator getOperator(String name) {
        int slash = name.indexOf('/');
        boolean isLocalName = (slash == -1);

        if (slash == 0) {
            // root catalog
            if (name.length() == 1) {
                if (getParent() != null) {
                    // we're not the root, so go to our parent
                    return getParent().getOperator(name);
                }
                // we're the root
                return owner;
            }

            // absolute path
            String myPath = owner.getPath();
            if (getParent() != null) {
                myPath += "/";
            }
            if (name.startsWith(myPath)) {
                // the path points to a descendant of this catalog, so grab the path relative to here
                name = name.substring(myPath.length());
            } else if (getParent() != null) {
                // the path doesn't start with our path, so go up the tree
                return getParent().getOperator(name);
            } else {
                // the path doesn't start with our path, and we are at the root, so nowhere else to look!
                return null;
            }
        }

        // relative path
        return getRelativeOperator(name, isLocalName);
    }

    private IOperator getRelativeOperator(String relativePath, boolean isLocalName) {
        String name;
        int nextSlash = relativePath.indexOf("/");
        if (nextSlash > -1) {
            // a descendant of a child catalog
            name = relativePath.substring(0, nextSlash);
            relativePath = relativePath.substring(nextSlash + 1);
        } else {
            // a child of this catalog
            name = relativePath;
            relativePath = null;
        }
        if ("..".equals(name)) {
            if (getParent() != null) {
                return getParent().getOperator(relativePath);
            } else {
                return null;
            }
        }
        IOperator operator = operatorsByName.get(name);
        if (relativePath != null && operator instanceof ICatalog) {
            // pass the path to the child catalog
            return ((ICatalog) operator).getOperator(relativePath);
        }
        if (operator == null && isLocalName && getParent() != null) {
            // if it's a local name, search up the tree
            return getParent().getOperator(name);
        }
        // we either found it here, or it doesn't exist
        return operator;
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
        String[] components = path.split("/");
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
}
