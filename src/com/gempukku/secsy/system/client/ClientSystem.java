package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClientSystem<E> {
    private EntityManager<E> entityManager;

    private Map<String, Client> clients = new HashMap<>();
    private Map<String, Set<Integer>> knownEntitiesOnClient = new HashMap<>();

    public void setEntityManager(EntityManager<E> entityManager) {
        this.entityManager = entityManager;
    }

    public void addClient(String clientId, Client<E> client) {

    }

    public void removeClient(String clientId) {

    }
}
