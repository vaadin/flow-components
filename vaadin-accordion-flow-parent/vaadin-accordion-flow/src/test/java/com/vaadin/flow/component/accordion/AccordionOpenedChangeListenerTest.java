/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.accordion;

import java.io.Serializable;
import java.util.OptionalInt;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion.OpenedChangeEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.flow.internal.nodefeature.PropertyChangeDeniedException;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class AccordionOpenedChangeListenerTest {

    private UI ui;
    private VaadinSession session;

    @SuppressWarnings("unchecked")
    private ComponentEventListener<OpenedChangeEvent> mockListener = Mockito
            .mock(ComponentEventListener.class);

    private ArgumentCaptor<OpenedChangeEvent> eventCaptor = ArgumentCaptor
            .forClass(OpenedChangeEvent.class);

    private Accordion accordion;

    @Before
    public void setup() {
        VaadinService service = Mockito.mock(VaadinService.class);
        DeploymentConfiguration deploymentConfig = Mockito
                .mock(DeploymentConfiguration.class);
        Mockito.when(deploymentConfig.isProductionMode()).thenReturn(false);
        Mockito.when(service.getDeploymentConfiguration())
                .thenReturn(deploymentConfig);

        session = Mockito.spy(new AlwaysLockedVaadinSession(service));
        ui = new UI();
        ui.getInternals().setSession(session);
        UI.setCurrent(ui);
        VaadinSession.setCurrent(session);

        accordion = new Accordion();
        accordion.add("Panel 0", new Span("Content 0"));
        accordion.add("Panel 1", new Span("Content 1"));
        accordion.add("Panel 2", new Span("Content 2"));
        accordion.addOpenedChangeListener(mockListener);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
        VaadinSession.setCurrent(null);
    }

    @Test
    public void openFromClient() {
        fakeClientPropertyChange("opened", 2);

        Mockito.verify(mockListener).onComponentEvent(eventCaptor.capture());
        OpenedChangeEvent event = eventCaptor.getValue();
        Assert.assertTrue(event.isFromClient());
        Assert.assertEquals(OptionalInt.of(2), event.getOpenedIndex());
    }

    @Test
    public void closeFromClient() {
        fakeClientPropertyChange("opened", 2);
        Mockito.reset(mockListener);

        fakeClientPropertyChange("opened", null);

        Mockito.verify(mockListener).onComponentEvent(eventCaptor.capture());
        OpenedChangeEvent event = eventCaptor.getValue();
        Assert.assertTrue(event.isFromClient());
        Assert.assertEquals(OptionalInt.empty(), event.getOpenedIndex());
    }

    @Test
    public void openFromServer() {
        accordion.open(1);

        Mockito.verify(mockListener).onComponentEvent(eventCaptor.capture());
        OpenedChangeEvent event = eventCaptor.getValue();
        Assert.assertFalse(event.isFromClient());
        Assert.assertEquals(OptionalInt.of(1), event.getOpenedIndex());
    }

    @Test
    public void closeFromServer() {
        accordion.open(1);
        Mockito.reset(mockListener);

        accordion.close();

        Mockito.verify(mockListener).onComponentEvent(eventCaptor.capture());
        OpenedChangeEvent event = eventCaptor.getValue();
        Assert.assertFalse(event.isFromClient());
        Assert.assertEquals(OptionalInt.empty(), event.getOpenedIndex());
    }

    private void fakeClientPropertyChange(String property, Serializable value) {
        try {
            accordion.getElement().getNode()
                    .getFeature(ElementPropertyMap.class)
                    .deferredUpdateFromClient(property, value).run();
        } catch (PropertyChangeDeniedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class AlwaysLockedVaadinSession extends VaadinSession {
        private final ReentrantLock lock = new ReentrantLock();

        private AlwaysLockedVaadinSession(VaadinService service) {
            super(service);
            lock();
        }

        @Override
        public Lock getLockInstance() {
            return lock;
        }
    }
}
