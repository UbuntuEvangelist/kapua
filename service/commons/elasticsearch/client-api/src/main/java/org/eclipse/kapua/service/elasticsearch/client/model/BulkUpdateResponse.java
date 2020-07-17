/*******************************************************************************
 * Copyright (c) 2017, 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.elasticsearch.client.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Bulk update response definition.
 * <p>
 * It bundles a {@link List} of {@link UpdateRequest}
 *
 * @since 1.0.0
 */
public class BulkUpdateResponse {

    List<UpdateResponse> responseList;

    /**
     * Adds an {@link UpdateResponse} to the {@link List}
     *
     * @param response The {@link UpdateResponse} to add.
     * @since 1.0.0
     */
    public void add(UpdateResponse response) {
        getResponse().add(response);
    }

    /**
     * Gets the {@link List} of {@link UpdateResponse}s.
     *
     * @return The {@link List} of {@link UpdateResponse}s.
     * @since 1.0.0
     */
    public List<UpdateResponse> getResponse() {
        if (responseList == null) {
            responseList = new ArrayList<>();
        }

        return responseList;
    }

    /**
     * Sets the {@link List} of {@link UpdateResponse}s.
     *
     * @param responseList The {@link List} of {@link UpdateResponse}s.
     * @since 1.0.0
     */
    public void setResponse(List<UpdateResponse> responseList) {
        this.responseList = responseList;
    }

}
