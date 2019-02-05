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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.gridpro.GridPro.EditColumn;
import com.vaadin.flow.function.SerializableFunction;

/**
 * Configuration for the editor of an edit column.
 *
 * @author Vaadin Ltd.
 * @param <T>
 *            the grid bean type
 */
public class EditColumnConfigurator<T> implements Serializable {

    private final EditColumn<T> column;

    /**
     * Creates a new configurator for the given column.
     *
     * @param column
     *            the column to edit, not <code>null</code>
     */
    public EditColumnConfigurator(EditColumn<T> column) {
        assert column != null;
        this.column = column;
    }

    private Column<T> configureColumn(ItemUpdater<T, String> itemUpdater,
            EditorType type, List<String> options) {
        column.setEditorType(type);
        column.setItemUpdater(itemUpdater);
        column.setOptions(options);

        return getColumn();
    }

    /**
     * Gets the column.
     *
     * @return the configured column
     */
    public Column<T> getColumn() {
        return column;
    }

    /**
     * Configures the column to have a text editor with the given item updater.
     *
     * @param itemUpdater
     *            the callback function that is called when item is changed.
     *            It receives two arguments: item, newValue.
     * @return the configured column
     *
     */
    public Column<T> text(ItemUpdater<T, String> itemUpdater) {
        return configureColumn(itemUpdater, EditorType.TEXT,
                Collections.emptyList());
    }

    /**
     * Configures the column to have a checkbox editor with the given item
     * updater.
     *
     * @param itemUpdater
     *            the callback function that is called when item is changed.
     *            It receives two arguments: item, newValue.
     * @return the configured column
     */
    public Column<T> checkbox(ItemUpdater<T, Boolean> itemUpdater) {
        ItemUpdater<T, String> wrapper = (item, value) -> itemUpdater
                .accept(item, Boolean.valueOf(value));

        return configureColumn(wrapper, EditorType.CHECKBOX,
                Collections.emptyList());
    }

    /**
     * Configures the column to have a select editor with the given item updater
     * and options.
     *
     * @param itemUpdater
     *            the callback function that is called when item is changed.
     *            It receives two arguments: item, newValue.
     * @param options
     *            options provided for the select editor
     * @return the configured column
     */
    public Column<T> select(ItemUpdater<T, String> itemUpdater,
            List<String> options) {
        Objects.requireNonNull(options);

        return configureColumn(itemUpdater, EditorType.SELECT, options);
    }

    /**
     * Configures the column to have a select editor with the given item updater
     * and options.
     *
     * @param itemUpdater
     *            the callback function that is called when item is changed.
     *            It receives two arguments: item, newValue.
     * @param options
     *            options provided for the select editor
     * @return the configured column
     */
    public Column<T> select(ItemUpdater<T, String> itemUpdater,
            String... options) {
        return select(itemUpdater, Arrays.asList(options));
    }

    /**
     * Configures the column to have a select editor with the given item
     * updater, enum type and string representation callback. All constants from
     * the given enum will be used, in their natural order. To exclude some
     * constants or use a different order, build the list of options manually
     * and use {@link #select(ItemUpdater, String...)}.
     *
     * @param <E>
     *            the enum type
     * @param itemUpdater
     *            the callback function that is called when item is changed.
     *            It receives two arguments: item and newValue.
     * @param enumType
     *            the enum class
     * @param getStringRepresentation
     *            callback used to get the string representation for each enum constant.
     * @return the configured column
     *
     * @throws IllegalArgumentException
     *             if any of the enum constants have the same string representation
     */
    public <E extends Enum<E>> Column<T> select(ItemUpdater<T, E> itemUpdater,
            Class<E> enumType,
            SerializableFunction<E, String> getStringRepresentation) {
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
     * Configures the column to have a select editor with the given item
     * updater, enum type using toString() as the string representation. All
     * constants from the given enum will be used, in their natural order. To
     * exclude some constants or use a different order, build the list of
     * options manually and use {@link #select(ItemUpdater, String...)}.
     *
     * @param <E>
     *            the enum type
     * @param itemUpdater
     *            the callback function that is called when item is changed.
     *            It receives two arguments: item and newValue.
     * @param enumType
     *            the enum class
     * @return the configured column
     */
    public <E extends Enum<E>> Column<T> select(ItemUpdater<T, E> itemUpdater,
            Class<E> enumType) {
        return select(itemUpdater, enumType, Object::toString);
    }
}
