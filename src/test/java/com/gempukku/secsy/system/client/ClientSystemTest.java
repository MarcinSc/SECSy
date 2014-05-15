package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityManager;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.Event;
import com.gempukku.secsy.SampleEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class ClientSystemTest {
    private ClientSystem<Event> clientSystem;
    private EntityManager<Event> entityManager;

    private Client<Event> client1;
    private EntityRef<Event> clientEntity1;

    private Client<Event> client2;
    private EntityRef<Event> clientEntity2;

    private EntityRef<Event> entity1;
    private EntityRef<Event> entity2;

    @Before
    public void setup() {
        entityManager = Mockito.mock(EntityManager.class);

        client1 = Mockito.mock(Client.class);
        clientEntity1 = Mockito.mock(EntityRef.class);

        client2 = Mockito.mock(Client.class);
        clientEntity2 = Mockito.mock(EntityRef.class);

        entity1 = Mockito.mock(EntityRef.class);
        entity2 = Mockito.mock(EntityRef.class);

        Mockito.when(entityManager.getEntityId(entity1)).thenReturn(1);
        Mockito.when(entityManager.getEntityId(entity2)).thenReturn(2);

        clientSystem = new ClientSystem<>();
        clientSystem.setEntityManager(entityManager);
    }

    @Test
    public void addingClientAddsRelevantEntities() {
        EntityRelevancyRuleMock<Event> relevancyRule = new EntityRelevancyRuleMock<>();
        relevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));

        clientSystem.addEntityRelevancyRule(relevancyRule);

        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verify(client1).updateEntity(1, entity1, Collections.singleton(relevancyRule));
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void entityRuleOperationsAddsOrRemovesRelevantEntities() {
        EntityRelevancyRuleMock<Event> relevancyRule = new EntityRelevancyRuleMock<>();
        relevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));

        clientSystem.addClient("1", clientEntity1, client1);
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        clientSystem.addEntityRelevancyRule(relevancyRule);

        Mockito.verify(client1).updateEntity(1, entity1, Collections.singleton(relevancyRule));
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        clientSystem.removeEntityRelevancyRule(relevancyRule);

        Mockito.verify(client1).removeEntity(1);
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void addingRuleDoesNotSendEntityToClientAfterItWasRemoved() {
        EntityRelevancyRuleMock<Event> relevancyRule = new EntityRelevancyRuleMock<>();
        relevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));

        clientSystem.addClient("1", clientEntity1, client1);
        clientSystem.removeClient("1");
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        clientSystem.addEntityRelevancyRule(relevancyRule);
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void addingClientSendsEntityTwiceIfRelevantForTwoRules() {
        EntityRelevancyRuleMock<Event> relevancyRule = new EntityRelevancyRuleMock<>();
        relevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));
        EntityRelevancyRuleMock<Event> relevancyRule2 = new EntityRelevancyRuleMock<>();
        relevancyRule2.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));

        clientSystem.addEntityRelevancyRule(relevancyRule);
        clientSystem.addEntityRelevancyRule(relevancyRule2);
        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verify(client1, Mockito.times(2)).updateEntity(1, entity1, new HashSet<>(Arrays.asList(relevancyRule, relevancyRule2)));
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void removingRuleDoesNotRemoveEntityIfRelevantForOtherRule() {
        EntityRelevancyRuleMock<Event> relevancyRule = new EntityRelevancyRuleMock<>();
        relevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));
        EntityRelevancyRuleMock<Event> relevancyRule2 = new EntityRelevancyRuleMock<>();
        relevancyRule2.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));

        clientSystem.addEntityRelevancyRule(relevancyRule);
        clientSystem.addEntityRelevancyRule(relevancyRule2);
        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verify(client1, Mockito.times(2)).updateEntity(1, entity1, new HashSet<>(Arrays.asList(relevancyRule, relevancyRule2)));
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        clientSystem.removeEntityRelevancyRule(relevancyRule);
        Mockito.verify(client1, Mockito.times(3)).updateEntity(1, entity1, new HashSet<>(Arrays.asList(relevancyRule2)));
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void addingClientSendsBothEntitiesIfRelevantForDifferentRules() {
        EntityRelevancyRuleMock<Event> relevancyRule = new EntityRelevancyRuleMock<>();
        relevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));
        EntityRelevancyRuleMock<Event> relevancyRule2 = new EntityRelevancyRuleMock<>();
        relevancyRule2.setClientRelevantEntities(clientEntity1, Collections.singleton(entity2));

        clientSystem.addEntityRelevancyRule(relevancyRule);
        clientSystem.addEntityRelevancyRule(relevancyRule2);
        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verify(client1).updateEntity(1, entity1, Collections.singleton(relevancyRule));
        Mockito.verify(client1).updateEntity(2, entity2, Collections.singleton(relevancyRule2));
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void addingClientDoesNotAddEntityIfRelevantForDifferentClient() {
        EntityRelevancyRuleMock<Event> relevancyRule = new EntityRelevancyRuleMock<>();
        relevancyRule.setClientRelevantEntities(clientEntity2, Collections.singleton(entity1));

        clientSystem.addEntityRelevancyRule(relevancyRule);
        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void sendingEventSendsItToClient() {
        EntityRelevancyRuleMock<Event> entityRelevancyRule = new EntityRelevancyRuleMock<>();
        entityRelevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));

        EventRelevancyRuleMock<Event> eventRelevancyRule = new EventRelevancyRuleMock<>();
        eventRelevancyRule.setRelevantClientEntityEvents(clientEntity1, Collections.<EntityRef<Event>, Class<? extends Event>>singletonMap(entity1, SampleEvent.class));

        clientSystem.addEntityRelevancyRule(entityRelevancyRule);
        clientSystem.addEventRelevancyRule(eventRelevancyRule);
        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verify(client1).updateEntity(1, entity1, Collections.singleton(entityRelevancyRule));
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        SampleEvent event = new SampleEvent();
        clientSystem.eventReceived(entity1, event);

        Mockito.verify(client1).sendEvent(1, entity1, event);
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void sendingEventDoesNotSendItToClientIfEntityNotRelevant() {
        EventRelevancyRuleMock<Event> eventRelevancyRule = new EventRelevancyRuleMock<>();
        eventRelevancyRule.setRelevantClientEntityEvents(clientEntity1, Collections.<EntityRef<Event>, Class<? extends Event>>singletonMap(entity1, SampleEvent.class));

        clientSystem.addEventRelevancyRule(eventRelevancyRule);
        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        SampleEvent event = new SampleEvent();
        clientSystem.eventReceived(entity1, event);

        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void sendingEventDoesNotSendItToClientIfEventRelevantForOtherClient() {
        EntityRelevancyRuleMock<Event> entityRelevancyRule = new EntityRelevancyRuleMock<>();
        entityRelevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));

        EventRelevancyRuleMock<Event> eventRelevancyRule = new EventRelevancyRuleMock<>();
        eventRelevancyRule.setRelevantClientEntityEvents(clientEntity2, Collections.<EntityRef<Event>, Class<? extends Event>>singletonMap(entity1, SampleEvent.class));

        clientSystem.addEntityRelevancyRule(entityRelevancyRule);
        clientSystem.addEventRelevancyRule(eventRelevancyRule);
        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verify(client1).updateEntity(1, entity1, Collections.singleton(entityRelevancyRule));
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        SampleEvent event = new SampleEvent();
        clientSystem.eventReceived(entity1, event);

        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void sendingEventSendsEntityIfBecomesRelevantDueToEvent() {
        EntityRelevancyRuleMock<Event> entityRelevancyRule = new EntityRelevancyRuleMock<>();
        entityRelevancyRule.setImpactingEvent(SampleEvent.class);

        EventRelevancyRuleMock<Event> eventRelevancyRule = new EventRelevancyRuleMock<>();
        eventRelevancyRule.setRelevantClientEntityEvents(clientEntity1, Collections.<EntityRef<Event>, Class<? extends Event>>singletonMap(entity1, SampleEvent.class));

        clientSystem.addEntityRelevancyRule(entityRelevancyRule);
        clientSystem.addEventRelevancyRule(eventRelevancyRule);
        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        entityRelevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));

        SampleEvent event = new SampleEvent();
        clientSystem.eventReceived(entity1, event);

        Mockito.verify(client1).updateEntity(1, entity1, Collections.singleton(entityRelevancyRule));
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void sendingEventDoesNotSendEntityIfEventNotImpactingRule() {
        EntityRelevancyRuleMock<Event> entityRelevancyRule = new EntityRelevancyRuleMock<>();

        EventRelevancyRuleMock<Event> eventRelevancyRule = new EventRelevancyRuleMock<>();
        eventRelevancyRule.setRelevantClientEntityEvents(clientEntity1, Collections.<EntityRef<Event>, Class<? extends Event>>singletonMap(entity1, SampleEvent.class));

        clientSystem.addEntityRelevancyRule(entityRelevancyRule);
        clientSystem.addEventRelevancyRule(eventRelevancyRule);
        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        entityRelevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));

        SampleEvent event = new SampleEvent();
        clientSystem.eventReceived(entity1, event);

        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void sendingEventRemovesEntityIfStopsBeingRelevantDueToEvent() {
        EntityRelevancyRuleMock<Event> entityRelevancyRule = new EntityRelevancyRuleMock<>();
        entityRelevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));
        entityRelevancyRule.setImpactingEvent(SampleEvent.class);

        EventRelevancyRuleMock<Event> eventRelevancyRule = new EventRelevancyRuleMock<>();
        eventRelevancyRule.setRelevantClientEntityEvents(clientEntity1, Collections.<EntityRef<Event>, Class<? extends Event>>singletonMap(entity1, SampleEvent.class));

        clientSystem.addEntityRelevancyRule(entityRelevancyRule);
        clientSystem.addEventRelevancyRule(eventRelevancyRule);
        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verify(client1).updateEntity(1, entity1, Collections.singleton(entityRelevancyRule));
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        entityRelevancyRule.setClientRelevantEntities(clientEntity1, null);

        SampleEvent event = new SampleEvent();
        clientSystem.eventReceived(entity1, event);

        Mockito.verify(client1).removeEntity(1);
        Mockito.verify(client1).sendEvent(1, entity1, event);
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void sendingEventDoesNotRemoveEntityIfEventNotImpactingRule() {
        EntityRelevancyRuleMock<Event> entityRelevancyRule = new EntityRelevancyRuleMock<>();
        entityRelevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));

        EventRelevancyRuleMock<Event> eventRelevancyRule = new EventRelevancyRuleMock<>();
        eventRelevancyRule.setRelevantClientEntityEvents(clientEntity1, Collections.<EntityRef<Event>, Class<? extends Event>>singletonMap(entity1, SampleEvent.class));

        clientSystem.addEntityRelevancyRule(entityRelevancyRule);
        clientSystem.addEventRelevancyRule(eventRelevancyRule);
        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verify(client1).updateEntity(1, entity1, Collections.singleton(entityRelevancyRule));
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        entityRelevancyRule.setClientRelevantEntities(clientEntity1, null);

        SampleEvent event = new SampleEvent();
        clientSystem.eventReceived(entity1, event);

        Mockito.verify(client1).sendEvent(1, entity1, event);
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void sendingEventDoesNotSendEventToClientAfterItWasRemoved() {
        EntityRelevancyRuleMock<Event> entityRelevancyRule = new EntityRelevancyRuleMock<>();
        entityRelevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));

        EventRelevancyRuleMock<Event> eventRelevancyRule = new EventRelevancyRuleMock<>();
        eventRelevancyRule.setRelevantClientEntityEvents(clientEntity1, Collections.<EntityRef<Event>, Class<? extends Event>>singletonMap(entity1, SampleEvent.class));

        clientSystem.addClient("1", clientEntity1, client1);
        clientSystem.removeClient("1");
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        clientSystem.addEntityRelevancyRule(entityRelevancyRule);
        clientSystem.addEventRelevancyRule(eventRelevancyRule);

        SampleEvent event = new SampleEvent();
        clientSystem.eventReceived(entity1, event);
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }

    @Test
    public void sendingEventDoesNotSendItToClientAfterEventRuleWasRemoved() {
        EntityRelevancyRuleMock<Event> entityRelevancyRule = new EntityRelevancyRuleMock<>();
        entityRelevancyRule.setClientRelevantEntities(clientEntity1, Collections.singleton(entity1));

        EventRelevancyRuleMock<Event> eventRelevancyRule = new EventRelevancyRuleMock<>();
        eventRelevancyRule.setRelevantClientEntityEvents(clientEntity1, Collections.<EntityRef<Event>, Class<? extends Event>>singletonMap(entity1, SampleEvent.class));

        clientSystem.addEntityRelevancyRule(entityRelevancyRule);
        clientSystem.addEventRelevancyRule(eventRelevancyRule);
        clientSystem.addClient("1", clientEntity1, client1);

        Mockito.verify(client1).updateEntity(1, entity1, Collections.singleton(entityRelevancyRule));
        Mockito.verifyNoMoreInteractions(client1, clientEntity1);

        clientSystem.removeEventRelevancyRule(eventRelevancyRule);

        SampleEvent event = new SampleEvent();
        clientSystem.eventReceived(entity1, event);

        Mockito.verifyNoMoreInteractions(client1, clientEntity1);
    }
}
