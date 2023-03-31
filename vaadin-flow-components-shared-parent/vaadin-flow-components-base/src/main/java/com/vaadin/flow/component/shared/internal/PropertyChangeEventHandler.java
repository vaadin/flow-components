/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.shared.internal;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.shared.Registration;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Internal class that provides shared functionality for handling property
 * change listeners. Not intended to be used publicly.
 */
public class PropertyChangeEventHandler<E extends ComponentEvent<?>>
        implements Serializable {

    private Registration propertyChangeListenerRegistration;

    private int listenerCount;

    private String propertyName;

    private Consumer<PropertyChangeEvent> fireComponentEvent;

    private Component component;

    private Class<E> eventType;

    public PropertyChangeEventHandler(String propertyName, Component component,
            Class<E> eventType,
            Consumer<PropertyChangeEvent> fireComponentEvent) {
        this.propertyName = propertyName;
        this.component = component;
        this.eventType = eventType;
        this.fireComponentEvent = fireComponentEvent;
    }

    public Registration addListener(ComponentEventListener<E> listener) {
        if (listenerCount == 0) {
            propertyChangeListenerRegistration = component.getElement()
                    .addPropertyChangeListener(propertyName,
                            fireComponentEvent::accept);
        }
        listenerCount++;
        Registration listenerRegistration = ComponentUtil.addListener(component,
                eventType, listener);
        return () -> {
            listenerCount--;
            if (listenerCount == 0) {
                propertyChangeListenerRegistration.remove();
            }
            listenerRegistration.remove();
        };
    }
}
