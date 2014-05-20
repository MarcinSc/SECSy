package com.gempukku.secsy.system.client.host;

import com.gempukku.secsy.EntityRef;

import java.util.Collection;

public interface ClientManager<E> {
    public void addClient(String clientId, EntityRef<E> clientEntity, Client<E> client);

    public void removeClient(String clientId);

    public void addEntityRelevancyRule(EntityRelevancyRule<E> entityRelevancyRule);

    public void removeEntityRelevancyRule(EntityRelevancyRule<E> entityRelevancyRule);

    public void entityRelevanceRuleUpdatedForClient(String clientId, EntityRelevancyRule<E> entityRelevancyRule);

    public void entityRelevanceRuleUpdatedForClient(String clientId, EntityRelevancyRule<E> entityRelevancyRule,
                                                    Collection<EntityRef<E>> directEntitiesToAdd, Collection<EntityRef<E>> directEntitiesToRemove);

    public void addEventRelevancyRule(EventRelevancyRule<E> eventRelevancyRule);

    public void removeEventRelevancyRule(EventRelevancyRule<E> eventRelevancyRule);
}
