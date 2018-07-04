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

package io.viewserver.sql;

import com.facebook.presto.sql.tree.*;

/**
 * Created by bemm on 22/11/15.
 */
public class SupportedExpressionRewriter<C> extends ExpressionRewriter<C> {
    @Override
    public Expression rewriteRow(Row node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteBetweenPredicate(BetweenPredicate node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteSearchedCaseExpression(SearchedCaseExpression node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteSimpleCaseExpression(SimpleCaseExpression node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteSubqueryExpression(SubqueryExpression node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteExtract(Extract node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteCurrentTime(CurrentTime node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteArrayConstructor(ArrayConstructor node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteSubscriptExpression(SubscriptExpression node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteLambdaExpression(LambdaExpression node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteCoalesceExpression(CoalesceExpression node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteNullIfExpression(NullIfExpression node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteIsNotNullPredicate(IsNotNullPredicate node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression rewriteIsNullPredicate(IsNullPredicate node, C context, ExpressionTreeRewriter<C> treeRewriter) {
        throw new UnsupportedOperationException();
    }
}
