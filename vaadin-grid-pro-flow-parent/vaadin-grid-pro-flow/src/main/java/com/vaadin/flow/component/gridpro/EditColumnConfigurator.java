/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.gridpro;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.gridpro.GridPro.EditColumn;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;

/**
 * Configuration for the editor of an edit column.
 *
 * @author Vaadin Ltd.
 * @param <T>
 *            the grid bean type
 */
public class EditColumnConfigurator<T> implements Serializable {

    private final EditColumn<T> column;
    private Registration attachRegistration;

    private boolean editModeRendererRequested = false;

    /**
     * Creates a new configurator for the given column.
     *
     * @param column
     *            the column to edit, not <code>null</code>
     */
    EditColumnConfigurator(EditColumn<T> column,
            ValueProvider<T, ?> valueProvider) {
        assert column != null;
        this.column = column;
        this.column.setValueProvider(valueProvider);
    }

    private Column<T> configureColumn(ItemUpdater<T, String> itemUpdater,
            EditorType type, List<String> options) {
        column.setEditorType(type);
        column.setItemUpdater(itemUpdater);
        column.setOptions(options);

        column.getElement().getNode()
                .runWhenAttached(ui -> ui.beforeClientResponse(column,
                        context -> UI.getCurrent().getPage().executeJs(
                                "window.Vaadin.Flow.gridProConnector.patchEditModeRenderer($0)",
                                column.getElement())));

        return getColumn();
    }

    private <V> Column<T> configureColumn(ValueProvider<T, V> valueProvider,
            ItemUpdater<T, String> itemUpdater, EditorType type,
            HasValueAndElement<?, V> editorField) {
        column.setEditorType(type);
        column.setItemUpdater(itemUpdater);
        column.setEditorField(editorField);
        column.setValueProvider(valueProvider);

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
     *            the callback function that is called when item is changed. It
     *            receives two arguments: item, newValue.
     * @return the configured column
     *
     */
    public Column<T> text(ItemUpdater<T, String> itemUpdater) {
        return configureColumn(itemUpdater, EditorType.TEXT,
                Collections.emptyList());
    }

    /**
     * Configures the column to have a custom editor component.
     * <p>
     * When editing starts, the editor's value is initialized with the same
     * presentation value that is used for the column. When committing the
     * editor value, the item updater is called to update the edited item with
     * the new value.
     *
     * @param component
     *            the editor component, which must be an implementation of
     *            {@link HasValueAndElement}
     * @param itemUpdater
     *            the callback function that is called when the editor value has
     *            changed. It receives the edited item, and the new value from
     *            the editor.
     * @return the configured column
     */
    public <V> Column<T> custom(HasValueAndElement<?, V> component,
            ItemUpdater<T, V> itemUpdater) {
        @SuppressWarnings("unchecked")
        ValueProvider<T, V> valueProvider = (ValueProvider<T, V>) column
                .getValueProvider();
        return custom(component, valueProvider, itemUpdater);
    }

    /**
     * Configures the column to have a custom editor component, using a custom
     * value provider.
     * <p>
     * When editing starts, the editor's value is initialized using the custom
     * value provider. When committing the editor value, the item updater is
     * called to update the edited item with the new value.
     *
     * @param component
     *            the editor component, which must be an implementation of
     *            {@link HasValueAndElement}
     * @param valueProvider
     *            the value provider that is used to initialize the editor value
     * @param itemUpdater
     *            the callback function that is called when the editor value has
     *            changed. It receives the edited item, and the new value from
     *            the editor.
     * @return the configured column
     */
    public <V> Column<T> custom(HasValueAndElement<?, V> component,
            ValueProvider<T, V> valueProvider, ItemUpdater<T, V> itemUpdater) {
        column.getElement().appendVirtualChild(component.getElement());
        if (attachRegistration != null) {
            attachRegistration.remove();
            attachRegistration = null;
        }
        // Need to call on attach to make sure that the edit mode renderer is
        // set in case the GridPro is detached and attached again
        attachRegistration = column.getElement()
                .addAttachListener(e -> setEditModeRenderer(component));

        // Calling setEditModeRenderer here in case the GridPro is already
        // attached and the column is added later
        // This is needed because in this case the attach listener is not called
        setEditModeRenderer(component);

        return configureColumn(valueProvider, (item, ignore) -> itemUpdater
                .accept(item, component.getValue()), EditorType.CUSTOM,
                component);
    }

    private <V> void setEditModeRenderer(HasValueAndElement<?, V> component) {
        if (editModeRendererRequested) {
            return;
        }
        editModeRendererRequested = true;
        column.getElement().getNode().runWhenAttached(ui -> {
            ui.beforeClientResponse(column, context -> {
                if (!editModeRendererRequested) {
                    return;
                }
                ui.getPage().executeJs(
                        "window.Vaadin.Flow.gridProConnector.setEditModeRenderer($0, $1)",
                        column.getElement(), component.getElement());
                editModeRendererRequested = false;
            });
        });
    }

    /**
     * Configures the column to have a checkbox editor with the given item
     * updater.
     *
     * @param itemUpdater
     *            the callback function that is called when item is changed. It
     *            receives two arguments: item, newValue.
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
     *            the callback function that is called when item is changed. It
     *            receives two arguments: item, newValue.
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
     *            the callback function that is called when item is changed. It
     *            receives two arguments: item, newValue.
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
     *            the callback function that is called when item is changed. It
     *            receives two arguments: item and newValue.
     * @param enumType
     *            the enum class
     * @param getStringRepresentation
     *            callback used to get the string representation for each enum
     *            constant.
     * @return the configured column
     *
     * @throws IllegalArgumentException
     *             if any of the enum constants have the same string
     *             representation
     */
    public <E extends Enum<E>> Column<T> select(ItemUpdater<T, E> itemUpdater,
            Class<E> enumType,
            SerializableFunction<E, String> getStringRepresentation) {
        Map<String, E> map = new HashMap<>();
        E[] items = enumType.getEnumConstants();
        List<String> itemsList = new ArrayList<>();

        for (E item : items) {
            String stringRepresentation = getStringRepresentation.apply(item);
            if (map.containsKey(stringRepresentation)) {
                throw new IllegalArgumentException("Enum constants "
                        + map.get(stringRepresentation) + " and " + item
                        + " both have the same string representation: "
                        + stringRepresentation);
            }
            map.put(stringRepresentation, item);
            itemsList.add(stringRepresentation);
        }

        ItemUpdater<T, String> wrapper = (item, value) -> itemUpdater
                .accept(item, map.get(value));

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
     *            the callback function that is called when item is changed. It
     *            receives two arguments: item and newValue.
     * @param enumType
     *            the enum class
     * @return the configured column
     */
    public <E extends Enum<E>> Column<T> select(ItemUpdater<T, E> itemUpdater,
            Class<E> enumType) {
        return select(itemUpdater, enumType, Object::toString);
    }
}
