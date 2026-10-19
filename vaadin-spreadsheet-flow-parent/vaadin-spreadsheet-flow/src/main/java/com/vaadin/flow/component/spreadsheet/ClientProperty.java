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
import com.vaadin.flow.function.SerializableFunction;

/**
 * A property of the spreadsheet's element, as the server sets it for the
 * client: the value last set, which the element is given at once, attached or
 * not. Flow sends the client the last value a round trip leaves, so setting one
 * several times in a round trip costs no more on the wire.
 * <p>
 * Each is declared with the value the client has before it is ever set, which
 * {@link #reset()} writes back. Declaring one writes nothing: the element is
 * given a value only once one is set.
 *
 * @param <T>
 *            the type of the value
 */
final class ClientProperty<T> implements Serializable {

    private final Element element;
    private final SerializableBiConsumer<Element, T> writer;
    private final T initial;
    private T value;

    private ClientProperty(Element element,
            SerializableBiConsumer<Element, T> writer, T initial) {
        this.element = element;
        this.writer = writer;
        this.initial = initial;
        this.value = initial;
    }

    /** The value last set, or the initial one before any is. */
    T get() {
        return value;
    }

    /** Keeps the value, and gives it to the element. */
    void set(T value) {
        this.value = value;
        writer.accept(element, value);
    }

    /**
     * Sets the value the client has before it is ever set: for what a sheet or
     * a workbook no longer shown left on the client.
     */
    void reset() {
        set(initial);
    }

    /** A true or false, sent as one. */
    static ClientProperty<Boolean> bool(Element element, String name,
            boolean initial) {
        return new ClientProperty<>(element,
                (el, v) -> el.setProperty(name, v.booleanValue()), initial);
    }

    /**
     * A whole number, sent as a number: an element property has no whole number
     * type of its own.
     */
    static ClientProperty<Integer> integer(Element element, String name,
            int initial) {
        return new ClientProperty<>(element,
                (el, v) -> el.setProperty(name, v.doubleValue()), initial);
    }

    /** A number with a fraction, sent as a number. */
    static ClientProperty<Float> decimal(Element element, String name,
            float initial) {
        return new ClientProperty<>(element,
                (el, v) -> el.setProperty(name, v.doubleValue()), initial);
    }

    /**
     * A text, sent as it is; {@code null} takes the property off the client.
     */
    static ClientProperty<String> string(Element element, String name,
            String initial) {
        return new ClientProperty<>(element, (el, v) -> el.setProperty(name, v),
                initial);
    }

    /**
     * An object, sent as JSON; {@code null} is JSON's null, which the client
     * starts from.
     */
    static <T> ClientProperty<T> json(Element element, String name) {
        return json(element, name, value -> value);
    }

    /**
     * An object, sent as JSON in the shape {@code wire} gives it - row heights
     * as runs of equal ones, say; {@code null} is JSON's null, which the client
     * starts from.
     */
    static <T> ClientProperty<T> json(Element element, String name,
            SerializableFunction<T, Object> wire) {
        return new ClientProperty<>(element,
                (el, v) -> el.setPropertyJson(name,
                        Serializer.toJson(v == null ? null : wire.apply(v))),
                null);
    }
}
