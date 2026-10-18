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

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiConsumer;

/**
 * A server-side cache of a value that mirrors itself to a client element
 * property on every set.
 *
 * <p>
 * Encapsulates the repeated {@code this.x = x;
 * getElement().setProperty(name, x)} pattern.
 *
 * <p>
 * Construction does <em>not</em> sync the initial value to the client: the
 * value is mirrored only on explicit setter calls.
 *
 * <p>
 * The {@link #set(Object)} method is the primary surface. Read-back via
 * {@link #get()} returns the last-set value. The mirror strategy is supplied at
 * construction as a {@link SerializableBiConsumer} that knows how to push the
 * value to the element (e.g. via {@code setProperty(name, value)} for
 * primitives, or {@code setPropertyJson(name, Serializer.toJson(value))} for
 * objects). Convenience factories below cover the common patterns.
 *
 * @param <T>
 *            the type of the mirrored value
 */
final class MirroredProperty<T> implements Serializable {

    private final Spreadsheet spreadsheet;
    private final SerializableBiConsumer<Element, T> mirror;
    private T value;

    /**
     * Creates a mirror with an explicit strategy. Use the factory methods
     * ({@link #bool}, {@link #serialized}) for common patterns.
     */
    MirroredProperty(Spreadsheet spreadsheet,
            SerializableBiConsumer<Element, T> mirror, T initial) {
        this.spreadsheet = spreadsheet;
        this.mirror = mirror;
        this.value = initial;
        // Intentionally NOT calling mirror.accept(element, initial) here.
        // The original setX() pattern only synced on set, never on field
        // initialization; Flow pushes initial state through its own
        // mechanism.
    }

    T get() {
        return value;
    }

    void set(T value) {
        this.value = value;
        mirror.accept(spreadsheet.getElement(), value);
    }

    // ----- factory methods for common patterns -----------------------------

    /**
     * Boolean mirror — pushes via {@code setProperty(name, boolean)}.
     */
    static MirroredProperty<Boolean> bool(Spreadsheet spreadsheet, String name,
            boolean initial) {
        return new MirroredProperty<>(spreadsheet,
                (el, v) -> el.setProperty(name, v.booleanValue()), initial);
    }

    /**
     * Integer mirror — pushes via {@code setProperty(name, double)} which
     * widens the int. Mirrors the existing call sites that pass {@code int}
     * primitives to {@code setProperty}.
     */
    static MirroredProperty<Integer> integer(Spreadsheet spreadsheet,
            String name, int initial) {
        return new MirroredProperty<>(spreadsheet,
                (el, v) -> el.setProperty(name, v.doubleValue()), initial);
    }

    /**
     * String mirror — pushes via {@code setProperty(name, String)}. Handles
     * {@code null} by passing {@code null} to {@code setProperty}, which clears
     * the property on the client.
     */
    static MirroredProperty<String> string(Spreadsheet spreadsheet, String name,
            String initial) {
        return new MirroredProperty<>(spreadsheet,
                (el, v) -> el.setProperty(name, v), initial);
    }

    /**
     * Object mirror — pushes via
     * {@code setPropertyJson(name, Serializer.toJson(value))} so the client
     * gets the value as JSON. The convention across Spreadsheet for
     * non-primitive properties.
     */
    static <T> MirroredProperty<T> serialized(Spreadsheet spreadsheet,
            String name, T initial) {
        return new MirroredProperty<>(spreadsheet,
                (el, v) -> el.setPropertyJson(name, Serializer.toJson(v)),
                initial);
    }
}
