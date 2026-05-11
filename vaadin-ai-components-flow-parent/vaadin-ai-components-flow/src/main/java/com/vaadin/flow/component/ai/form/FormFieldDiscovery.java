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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasValue;

/**
 * Walks a form's component tree and produces an identifier from a label.
 * <p>
 * Discovery is a one-shot operation performed at controller construction;
 * neither the field set nor the identifier derivation is re-run at request
 * time.
 */
final class FormFieldDiscovery {

    private FormFieldDiscovery() {
    }

    /**
     * Collects every {@link HasValue} component found by walking the given
     * component subtree breadth-first through any {@link HasComponents}
     * children.
     *
     * @param root
     *            the component to walk, not {@code null}
     * @return the discovered fields in document order
     */
    static List<HasValue<?, ?>> collectFields(Component root) {
        List<HasValue<?, ?>> sink = new ArrayList<>();
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

    /**
     * Normalizes a label to an ASCII identifier: lowercases, replaces runs of
     * non-{@code [a-z0-9_]} characters with a single underscore, and trims
     * leading/trailing underscores. Returns an empty string for {@code null}
     * or labels that collapse to nothing.
     *
     * @param label
     *            the original label, possibly {@code null}
     * @return the normalized identifier
     */
    static String normalize(String label) {
        if (label == null) {
            return "";
        }
        String lowered = label.toLowerCase();
        StringBuilder b = new StringBuilder(lowered.length());
        boolean lastWasSep = true;
        for (int i = 0; i < lowered.length(); i++) {
            char c = lowered.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_') {
                b.append(c);
                lastWasSep = false;
            } else if (!lastWasSep) {
                b.append('_');
                lastWasSep = true;
            }
        }
        while (b.length() > 0 && b.charAt(b.length() - 1) == '_') {
            b.deleteCharAt(b.length() - 1);
        }
        while (b.length() > 0 && b.charAt(0) == '_') {
            b.deleteCharAt(0);
        }
        return b.toString();
    }
}
