/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.util.Collection;
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

    private final Map<Element, Registration> childDetachListenerMap = new HashMap<>();

    @Override
    public void onDetach(ElementDetachEvent e) {
        var detachedElement = e.getSource();
        getDirectChildren().stream()
                .filter(childComponent -> Objects.equals(detachedElement,
                        childComponent.getElement()))
                .findAny().ifPresent(detachedChild -> {
                    // The child was removed from the component

                    // Remove the registration for the child detach listener
                    childDetachListenerMap.get(detachedChild.getElement())
                            .remove();
                    childDetachListenerMap.remove(detachedChild.getElement());

                    removeChild(detachedChild);
                });
    }

    void refreshListeners() {
        getDirectChildren().forEach(child -> {
            Element childElement = child.getElement();
            if (!childDetachListenerMap.containsKey(childElement)) {
                childDetachListenerMap.put(childElement,
                        childElement.addDetachListener(this));
            }
        });
    }

    abstract void removeChild(Component child);

    abstract Collection<Component> getDirectChildren();
}
