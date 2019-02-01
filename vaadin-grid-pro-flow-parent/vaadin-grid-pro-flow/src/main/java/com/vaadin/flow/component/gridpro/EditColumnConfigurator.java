package com.vaadin.flow.component.gridpro;

/*
 * #%L
 * Vaadin GridPro
 * %%
 * Copyright (C) 2018 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.function.SerializableFunction;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


/**
 * Configuration class with common available properties for different types of edit columns used
 * inside a {@link GridPro}.
 *
 * @author Vaadin Ltd.
 */
public class EditColumnConfigurator<T> implements Serializable {

    private ItemUpdater<T, String> itemUpdater;
    private EditorType type;
    private List<String> options;

    private EditColumnConfigurator(ItemUpdater<T, String> itemUpdater, EditorType type, List<String> options) {
        this.itemUpdater = itemUpdater;
        this.type = type;
        this.options = options;
    }

    protected EditorType getType() {
        return this.type;
    }

    protected ItemUpdater<T, String> getItemUpdater() {
        return this.itemUpdater;
    }

    protected List<String> getOptions() {
        return this.options;
    }

    /**
     * Constructs a new column configurator with text editor preset for column creation.
     *
     * @param <T>
     *            the grid bean type
     * @param itemUpdater
     *            the callback function that is called when item is changed.
     *            It receives two arguments: item, newValue.
     * @return the instance of EditColumnConfigurator
     *
     */
    public static <T> EditColumnConfigurator<T> text(ItemUpdater<T, String> itemUpdater) {
        return new EditColumnConfigurator<>(itemUpdater, EditorType.TEXT, Collections.emptyList());
    }

    /**
     * Constructs a new column configurator with checkbox editor preset for column creation.
     *
     * @param <T>
     *            the grid bean type
     * @param itemUpdater
     *            the callback function that is called when item is changed.
     *            It receives two arguments: item and newValue.
     * @return the instance of EditColumnConfigurator
     */
    public static <T> EditColumnConfigurator<T> checkbox(ItemUpdater<T, Boolean> itemUpdater) {
        ItemUpdater<T, String> wrapper = (item, value) -> itemUpdater.accept(item, Boolean.valueOf(value));

        return new EditColumnConfigurator<>(wrapper, EditorType.CHECKBOX, Collections.emptyList());
    }

    /**
     * Constructs a new column configurator with select editor preset for column creation.
     *
     * @param <T>
     *            the grid bean type
     * @param itemUpdater
     *            the callback function that is called when item is changed.
     *            It receives two arguments: item and newValue.
     * @param options
     *            options provided for the select editor type
     * @return the instance of EditColumnConfigurator
     */
    public static <T> EditColumnConfigurator<T> select(ItemUpdater<T, String> itemUpdater, List<String> options) {
        Objects.requireNonNull(options);

        return new EditColumnConfigurator<>(itemUpdater, EditorType.SELECT, options);
    }

    /**
     * Constructs a new column configurator with select editor preset for column creation.
     *
     * @param <T>
     *            the grid bean type
     * @param itemUpdater
     *            the callback function that is called when item is changed.
     *            It receives two arguments: item and newValue.
     * @param options
     *            options provided for the select editor type
     * @return the instance of EditColumnConfigurator
     */
    public static <T> EditColumnConfigurator<T> select(ItemUpdater<T, String> itemUpdater, String ...options) {
        return select(itemUpdater, Arrays.asList(options));
    }

    /**
     * Constructs a new column configurator with select editor preset for column creation based on an enum.
     *
     * @param <T>
     *            the grid bean type
     * @param <E>
     *            the enum type
     * @param enumType
     *            the enum class
     * @param getStringRepresentation
     *            callback used to get the string representation for each enum constant.
     * @param itemUpdater
     *            the callback function that is called when item is changed.
     *            It receives two arguments: item and newValue.
     * @return the instance of EditColumnConfigurator
     * @throws IllegalArgumentException
     *             if any of the enum constants have the same string representation
     */
    public static <T, E extends Enum<E>> EditColumnConfigurator<T> select(ItemUpdater<T, E> itemUpdater, Class<E> enumType, SerializableFunction<E, String> getStringRepresentation) {
        Map<String, E> map = new HashMap<>();
        E[] items = enumType.getEnumConstants();
        List<String> itemsList = new ArrayList<>();

        for(E item: items) {
            String stringRepresentation = getStringRepresentation.apply(item);
            if (map.containsKey(stringRepresentation)) {
                throw new IllegalArgumentException("Enum constants " +
                        map.get(stringRepresentation) + " and " +
                        item + " both have the same string representation: " + stringRepresentation);
            }
            map.put(stringRepresentation, item);
            itemsList.add(stringRepresentation);
        }

        ItemUpdater<T, String> wrapper = (item, value) -> itemUpdater.accept(item, map.get(value));

        return select(wrapper, itemsList);
    }

    /**
     * Constructs a new column configurator with select editor preset for column creation based the toString() values of an enum.
     *
     * @param <T>
     *            the grid bean type
     * @param <E>
     *            the enum type
     * @param enumType
     *            the enum class
     * @param itemUpdater
     *            the callback function that is called when item is changed.
     *            It receives two arguments: item and newValue.
     * @return the instance of EditColumnConfigurator
     */
    public static <T, E extends Enum<E>> EditColumnConfigurator<T> select(ItemUpdater<T, E> itemUpdater, Class<E> enumType) {
        return select(itemUpdater, enumType, Object::toString);
    }
}

