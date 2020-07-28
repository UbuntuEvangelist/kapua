/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.elasticsearch.client;

import org.eclipse.kapua.service.elasticsearch.client.configuration.ElasticsearchClientConfiguration;

import java.io.Closeable;

/**
 * {@link ElasticsearchClient} base implementation.
 *
 * @since 1.0.0
 */
public abstract class AbstractElasticsearchClient<C extends Closeable> implements ElasticsearchClient<C> {

    protected String clientType;

    private C client;

    private ElasticsearchClientConfiguration clientConfiguration;
    private ModelContext modelContext;
    private QueryConverter modelConverter;

    protected AbstractElasticsearchClient(String clientType) {
        this.clientType = clientType;
    }

    @Override
    public C getClient() {
        return client;
    }

    @Override
    public ElasticsearchClient<C> withClient(C client) {
        this.client = client;
        return this;
    }

    @Override
    public ElasticsearchClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    @Override
    public AbstractElasticsearchClient<C> withClientConfiguration(ElasticsearchClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
        return this;
    }

    @Override
    public ModelContext getModelContext() {
        return modelContext;
    }

    @Override
    public AbstractElasticsearchClient<C> withModelContext(ModelContext modelContext) {
        this.modelContext = modelContext;
        return this;
    }

    @Override
    public QueryConverter getModelConverter() {
        return modelConverter;
    }

    @Override
    public AbstractElasticsearchClient<C> withModelConverter(QueryConverter queryConverter) {
        this.modelConverter = queryConverter;
        return this;
    }

}
