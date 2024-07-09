/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.treegrid;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class CollapseEvent<T, C extends Component> extends ComponentEvent<C> {

    private final Collection<T> items;

    /**
     * CollapseEvent base constructor.
     *
     * @param source
     *            the source component
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     * @param items
     *            Collapsed items
     * @see ComponentEvent
     */
    public CollapseEvent(C source, boolean fromClient, Collection<T> items) {
        super(source, fromClient);
        this.items = Optional.ofNullable(items).orElse(Collections.emptyList());
    }

    public Collection<T> getItems() {
        return items;
    }
}
