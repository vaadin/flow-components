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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.accordion.Accordion.OpenedChangeEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.flow.internal.nodefeature.PropertyChangeDeniedException;
import com.vaadin.tests.MockUIExtension;

class AccordionOpenedChangeListenerTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @SuppressWarnings("unchecked")
    private ComponentEventListener<OpenedChangeEvent> mockListener = Mockito
            .mock(ComponentEventListener.class);

    private ArgumentCaptor<OpenedChangeEvent> eventCaptor = ArgumentCaptor
            .forClass(OpenedChangeEvent.class);

    private Accordion accordion;

    @BeforeEach
    void setup() {
        accordion = new Accordion();
        accordion.add("Panel 0", new Span("Content 0"));
        accordion.add("Panel 1", new Span("Content 1"));
        accordion.add("Panel 2", new Span("Content 2"));
        accordion.addOpenedChangeListener(mockListener);
    }

    @Test
    void openFromClient() {
        fakeClientPropertyChange("opened", 2);

        Mockito.verify(mockListener).onComponentEvent(eventCaptor.capture());
        OpenedChangeEvent event = eventCaptor.getValue();
        Assertions.assertTrue(event.isFromClient());
        Assertions.assertEquals(OptionalInt.of(2), event.getOpenedIndex());
    }

    @Test
    void closeFromClient() {
        fakeClientPropertyChange("opened", 2);
        Mockito.reset(mockListener);

        fakeClientPropertyChange("opened", null);

        Mockito.verify(mockListener).onComponentEvent(eventCaptor.capture());
        OpenedChangeEvent event = eventCaptor.getValue();
        Assertions.assertTrue(event.isFromClient());
        Assertions.assertEquals(OptionalInt.empty(), event.getOpenedIndex());
    }

    @Test
    void openFromServer() {
        accordion.open(1);

        Mockito.verify(mockListener).onComponentEvent(eventCaptor.capture());
        OpenedChangeEvent event = eventCaptor.getValue();
        Assertions.assertFalse(event.isFromClient());
        Assertions.assertEquals(OptionalInt.of(1), event.getOpenedIndex());
    }

    @Test
    void closeFromServer() {
        accordion.open(1);
        Mockito.reset(mockListener);

        accordion.close();

        Mockito.verify(mockListener).onComponentEvent(eventCaptor.capture());
        OpenedChangeEvent event = eventCaptor.getValue();
        Assertions.assertFalse(event.isFromClient());
        Assertions.assertEquals(OptionalInt.empty(), event.getOpenedIndex());
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
}
