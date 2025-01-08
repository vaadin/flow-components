/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementDetachEvent;
import com.vaadin.flow.dom.ElementDetachListener;
import com.vaadin.flow.shared.Registration;

public abstract class DashboardChildDetachHandler
        implements ElementDetachListener {

    private final Component component;

    private final Map<Element, Registration> childDetachListenerMap = new HashMap<>();

    DashboardChildDetachHandler(Component component) {
        this.component = component;
    }

    @Override
    public void onDetach(ElementDetachEvent e) {
        var detachedElement = e.getSource();
        var childDetachedFromContainer = component.getElement().getChildren()
                .noneMatch(containerChild -> Objects.equals(detachedElement,
                        containerChild));
        if (childDetachedFromContainer) {
            // The child was removed from the component
            // Remove the registration for the child detach listener
            childDetachListenerMap.get(detachedElement).remove();
            childDetachListenerMap.remove(detachedElement);
            detachedElement.getComponent().ifPresent(this::removeChild);
        }
    }

    void refreshListeners() {
        component.getChildren().forEach(child -> {
            Element childElement = child.getElement();
            if (!childDetachListenerMap.containsKey(childElement)) {
                childDetachListenerMap.put(childElement,
                        childElement.addDetachListener(this));
            }
        });
    }

    abstract void removeChild(Component child);
}
