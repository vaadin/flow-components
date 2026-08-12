/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasValue;

/**
 * Walks a form's component tree and collects every {@link HasValue} component
 * into a flat list in document order.
 *
 * @author Vaadin Ltd
 */
final class FormFieldDiscovery {

    private FormFieldDiscovery() {
    }

    /**
     * Collects every {@link HasValue} component reachable from the given root,
     * recursing through any {@link HasComponents} children so layouts
     * containing layouts are handled.
     *
     * @param root
     *            the component to walk, not {@code null}
     * @return the discovered fields in document order
     */
    static List<HasValue<?, ?>> collectFields(Component root) {
        Objects.requireNonNull(root, "Root must not be null");
        var sink = new ArrayList<HasValue<?, ?>>();
        collect(root, sink);
        return sink;
    }

    private static void collect(Component component,
            List<HasValue<?, ?>> sink) {
        component.getChildren().forEach(child -> {
            // A component that is both HasValue and HasComponents is treated
            // as a leaf field — its children are considered part of the
            // field's internal composition, not separate form fields.
            if (child instanceof HasValue<?, ?> hv) {
                sink.add(hv);
            } else if (child instanceof HasComponents) {
                collect(child, sink);
            }
        });
    }
}
