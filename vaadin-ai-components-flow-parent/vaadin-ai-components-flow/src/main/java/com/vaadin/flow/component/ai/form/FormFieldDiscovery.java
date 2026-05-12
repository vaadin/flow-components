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
            if (child instanceof HasValue<?, ?> hv) {
                sink.add(hv);
            }
            if (child instanceof HasComponents) {
                collect(child, sink);
            }
        });
    }
}
