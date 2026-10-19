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

import com.vaadin.flow.dom.Element;

/**
 * A map property of the spreadsheet's element that the server never holds as
 * {@code null}: setting {@code null} empties the map in place, for whoever
 * holds it, and sends the client JSON's null, which it starts from. A map set
 * replaces the one held. Like a {@link ClientProperty}, the element is given
 * the value at once, and declaring one writes nothing.
 *
 * @param <K>
 *            the type of the keys
 * @param <V>
 *            the type of the values
 */
final class ClientMapProperty<K, V> implements Serializable {

    private final Element element;
    private final String name;
    private HashMap<K, V> value = new HashMap<>();

    ClientMapProperty(Element element, String name) {
        this.element = element;
        this.name = name;
    }

    /** The map held: empty, never {@code null}, before one is set. */
    HashMap<K, V> get() {
        return value;
    }

    /**
     * Holds the map, or empties the one held for {@code null}, and gives the
     * element the map as JSON.
     */
    void set(HashMap<K, V> input) {
        if (input == null) {
            value.clear();
        } else {
            value = input;
        }
        element.setPropertyJson(name, Serializer.toJson(input));
    }

    /** Empties the map, as the client starts with none. */
    void reset() {
        set(null);
    }
}
