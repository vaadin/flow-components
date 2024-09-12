/**
 * Copyright 2000-2024 Vaadin Ltd.
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
import com.vaadin.flow.internal.NullOwner;
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
        if (isComponentDetaching()) {
            return;
        }
        var detachedElement = e.getSource();
        component.getChildren()
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

    private boolean isComponentDetaching() {
        return component.isAttached()
                && !NullOwner.get()
                        .equals(component.getElement().getNode().getOwner())
                && !component.getElement().getNode().getOwner()
                        .hasNode(component.getElement().getNode());
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
