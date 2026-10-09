/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.HashMap;

/**
 * A sibling of {@link MirroredProperty} for HashMap fields whose setter has
 * <em>null-coalescing</em> semantics: passing {@code null} clears the existing
 * map in place (preserving the reference) rather than nulling the field;
 * passing a non-null map replaces the reference. In both cases the supplied
 * value (which may be {@code null}) is serialized and pushed to the client.
 *
 * <p>
 * Captures the recurring shape in {@link Spreadsheet}:
 *
 * <pre>{@code
 * if (input == null) {
 *     this.field.clear();
 * } else {
 *     this.field = input;
 * }
 * getElement().setProperty(name, Serializer.serialize(input));
 * }</pre>
 *
 * <p>
 * Deliberately a separate class rather than a {@link MirroredProperty} variant
 * because the semantic differs in two ways: (1) the field has a non-null
 * default (an empty HashMap) instead of a typed initial value, and (2) the
 * {@code set(null)} branch mutates state rather than replacing it. Trying to
 * shoehorn this into {@link MirroredProperty}'s {@code BiConsumer} strategy
 * would obscure both differences.
 *
 * <p>
 * Construction does NOT eagerly mirror the empty map to the client — same
 * contract as {@link MirroredProperty}. The Flow framework handles initial
 * state push.
 */
final class MirroredNullableMap<K, V> implements Serializable {

    private final Spreadsheet spreadsheet;
    private final String propertyName;
    private HashMap<K, V> value = new HashMap<K, V>();

    MirroredNullableMap(Spreadsheet spreadsheet, String propertyName) {
        this.spreadsheet = spreadsheet;
        this.propertyName = propertyName;
    }

    HashMap<K, V> get() {
        return value;
    }

    /**
     * If {@code input} is {@code null}, clears the existing map in place
     * (callers holding the reference see an empty map). Otherwise replaces the
     * reference with {@code input}. Either way, pushes the serialized
     * {@code input} (which may be {@code null}) to the client element property
     * — the server-side cache and the client-side state can briefly diverge
     * after a null call (server has empty map, client sees null).
     */
    void set(HashMap<K, V> input) {
        if (input == null) {
            value.clear();
        } else {
            value = input;
        }
        spreadsheet.getElement().setProperty(propertyName,
                Serializer.serialize(input));
    }
}
