/*******************************************************************************
 * Copyright (c) 2016, 2022 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.commons.configuration;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.jpa.AbstractEntityCacheFactory;
import org.eclipse.kapua.commons.jpa.EntityManagerFactory;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.KapuaEntity;
import org.eclipse.kapua.model.KapuaEntityAttributes;
import org.eclipse.kapua.model.KapuaEntityCreator;
import org.eclipse.kapua.model.KapuaEntityFactory;
import org.eclipse.kapua.model.config.metatype.KapuaTocd;
import org.eclipse.kapua.model.domain.Domain;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaListResult;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.model.query.predicate.AttributePredicate.Operator;
import org.eclipse.kapua.service.KapuaEntityService;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountFactory;
import org.eclipse.kapua.service.account.AccountListResult;
import org.eclipse.kapua.service.account.AccountQuery;
import org.eclipse.kapua.service.account.AccountService;

import java.util.Map;

public abstract class AbstractKapuaConfigurableResourceLimitedService<E extends KapuaEntity, C extends KapuaEntityCreator<E>, S extends KapuaEntityService<E, C>, L extends KapuaListResult<E>, Q extends KapuaQuery, F extends KapuaEntityFactory<E, C, Q, L>>
        extends AbstractKapuaConfigurableService {

    private final Class<S> serviceClass;
    private final Class<F> factoryClass;

    /**
     * @deprecated this constructor will be removed in a next release (may be)
     */
    @Deprecated
    protected AbstractKapuaConfigurableResourceLimitedService(
            String pid,
            Domain domain,
            EntityManagerFactory entityManagerFactory,
            Class<S> serviceClass,
            Class<F> factoryClass) {
        this(pid, domain, entityManagerFactory, null, serviceClass, factoryClass);
    }

    protected AbstractKapuaConfigurableResourceLimitedService(
            String pid,
            Domain domain,
            EntityManagerFactory entityManagerFactory,
            AbstractEntityCacheFactory abstractCacheFactory,
            Class<S> serviceClass,
            Class<F> factoryClass) {
        super(pid, domain, entityManagerFactory, abstractCacheFactory);
        this.serviceClass = serviceClass;
        this.factoryClass = factoryClass;
    }

    @Override
    protected boolean validateNewConfigValuesCoherence(KapuaTocd ocd, Map<String, Object> updatedProps, KapuaId scopeId, KapuaId parentId) throws KapuaException {
        super.validateNewConfigValuesCoherence(ocd, updatedProps, scopeId, parentId);
        int availableChildEntitiesWithNewConfig = allowedChildEntities(scopeId, null, updatedProps);
        if (availableChildEntitiesWithNewConfig < 0) {
            throw new KapuaConfigurationException(KapuaConfigurationErrorCodes.SELF_LIMIT_EXCEEDED_IN_CONFIG);
        }
        if (parentId != null) {
            int availableParentEntitiesWithCurrentConfig = allowedChildEntities(parentId, scopeId);
            if (availableParentEntitiesWithCurrentConfig - availableChildEntitiesWithNewConfig < 0) {
                throw new KapuaConfigurationException(KapuaConfigurationErrorCodes.PARENT_LIMIT_EXCEEDED_IN_CONFIG);
            }
        }
        return true;
    }

    protected int allowedChildEntities(KapuaId scopeId) throws KapuaException {
        return allowedChildEntities(scopeId, null, null);
    }

    protected int allowedChildEntities(KapuaId scopeId, KapuaId targetScopeId) throws KapuaException {
        return allowedChildEntities(scopeId, targetScopeId, null);
    }

    /**
     * @param scopeId       The {@link KapuaId} of the account to be tested
     * @param targetScopeId Optional scopeId of the child account to be excluded when validating the new configuration for that scopeId.
     * @param configuration The configuration to be tested. If null will be read
     *                      from the current service configuration; otherwise the passed configuration
     *                      will be used in the test
     * @return the number of child accounts spots still available
     * @throws KapuaException
     */
    protected int allowedChildEntities(KapuaId scopeId, KapuaId targetScopeId, Map<String, Object> configuration) throws KapuaException {
        KapuaLocator locator = KapuaLocator.getInstance();
        S service = locator.getService(serviceClass);
        F factory = locator.getFactory(factoryClass);
        AccountFactory accountFactory = locator.getFactory(AccountFactory.class);
        AccountService accountService = locator.getService(AccountService.class);

        Map<String, Object> finalConfig = configuration == null ? getConfigValues(scopeId, false) : configuration;
        boolean allowInfiniteChildEntities = (boolean) finalConfig.get("infiniteChildEntities");
        if (!allowInfiniteChildEntities) {
            return KapuaSecurityUtils.doPrivileged(() -> {
                Q countQuery = factory.newQuery(scopeId);

                // Current used entities
                long currentUsedEntities = service.count(countQuery);

                AccountQuery childAccountsQuery = accountFactory.newQuery(scopeId);
                // Exclude the scope that is under config update
                if (targetScopeId != null) {
                    childAccountsQuery.setPredicate(childAccountsQuery.attributePredicate(KapuaEntityAttributes.ENTITY_ID, targetScopeId, Operator.NOT_EQUAL));
                }

                AccountListResult childAccounts = accountService.query(childAccountsQuery);
                // Resources assigned to children
                long childCount = 0;
                for (Account childAccount : childAccounts.getItems()) {
                    Map<String, Object> childConfigValues = getConfigValues(childAccount);
                    // maxNumberChildEntities can be null if such property is disabled via the
                    // isPropertyEnabled() method in the service implementation. In such case,
                    // it makes sense to treat the service as it had 0 available entities
                    Integer maxNumberChildEntities = (Integer) childConfigValues.get("maxNumberChildEntities");
                    childCount += maxNumberChildEntities != null ? maxNumberChildEntities : 0;
                }

                // Max allowed for this account
                int maxChildAccounts = (int) finalConfig.get("maxNumberChildEntities");
                return (int) (maxChildAccounts - currentUsedEntities - childCount);
            });
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Gets the scoped configuration values from the given {@link Account}.
     * This method defaults to {@link Account#getId()}, but implementations can change it to use other attributes.
     *
     * @param account The account from which get the id.
     * @return The scoped configurations for the given {@link Account}.
     * @throws KapuaException
     * @since 1.0.0
     */
    protected Map<String, Object> getConfigValues(Account account) throws KapuaException {
        return getConfigValues(account.getId());
    }

}
