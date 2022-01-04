/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.editor;

import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.ExecutionContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.AbstractGridExtension;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertySet;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;

/**
 * Implementation of {@code Editor} interface.
 *
 * @param <T>
 *            the grid bean type
 */
public class EditorImpl<T> extends AbstractGridExtension<T>
        implements Editor<T> {

    /**
     * This property is used in the Grid <code>dom-if</code> template for
     * setting the editor component.
     */
    private static final String EDITING = "_editing";

    private final Map<Class<?>, List<?>> listeners = new HashMap<>();
    private SerializableConsumer<ExecutionContext> editItemRequest;
    private Binder<T> binder;
    private T edited;
    private boolean isBuffered;

    public EditorImpl(Grid<T> grid, PropertySet<T> propertySet) {
        super(grid);

        if (propertySet != null) {
            binder = Binder.withPropertySet(propertySet);
        }

        getGrid().addItemClickListener(this::handleItemClick);
    }

    @Override
    public Editor<T> setBinder(Binder<T> binder) {
        Objects.requireNonNull(binder, "Binder can't edit null");
        this.binder = binder;
        return this;
    }

    @Override
    public Binder<T> getBinder() {
        if (binder == null) {
            LoggerFactory.getLogger(EditorImpl.class).warn(
                    "The editor binder is null. You have to set it explicitly.");
        }
        return binder;
    }

    @Override
    public Editor<T> setBuffered(boolean buffered) {
        isBuffered = buffered;
        return this;
    }

    @Override
    public boolean isBuffered() {
        return isBuffered;
    }

    @Override
    public boolean isOpen() {
        return edited != null;
    }

    @Override
    public boolean save() {
        if (isOpen() && isBuffered()) {
            getBinder().validate();
            if (getBinder().writeBeanIfValid(edited)) {
                fireSaveEvent(new EditorSaveEvent<>(this, edited));
                close();
                return true;
            }
        }
        return false;
    }

    @Override
    public void cancel() {
        fireCancelEvent(new EditorCancelEvent<>(this, edited));
        close();
    }

    @Override
    public void closeEditor() {
        if (isOpen() && isBuffered()) {
            throw new UnsupportedOperationException(
                    "Buffered editor should be closed using save() or cancel()");
        }
        close();
    }

    @Override
    public void editItem(T item) {
        Objects.requireNonNull(item, "Editor can't edit null");

        final T it = item;
        if (editItemRequest == null) {
            editItemRequest = context -> {
                requestEditItem(it);
                editItemRequest = null;
            };
            getGrid().getElement().getNode().runWhenAttached(
                    ui -> ui.getInternals().getStateTree().beforeClientResponse(
                            getGrid().getElement().getNode(), editItemRequest));
        }
    }

    private void requestEditItem(T item) {
        validate(item);

        close();
        edited = item;

        refresh(item);

        if (isBuffered()) {
            binder.readBean(item);
        } else {
            binder.setBean(item);
        }

        fireOpenEvent(new EditorOpenEvent<>(this, edited));
    }

    @Override
    public void refresh() {
        if (!isOpen()) {
            return;
        }
        refresh(edited);
    }

    @Override
    public T getItem() {
        return edited;
    }

    @Override
    public Grid<T> getGrid() {
        return super.getGrid();
    }

    @Override
    public void generateData(T item, JsonObject jsonObject) {
        if (item != null && item.equals(edited)) {
            jsonObject.put(EDITING, true);
        } else {
            jsonObject.remove(EDITING);
        }
    }

    private void close() {
        if (edited != null) {
            T oldEdited = edited;
            edited = null;
            refresh(oldEdited);
            fireCloseEvent(new EditorCloseEvent<>(this, oldEdited));
        }
    }

    private void handleItemClick(ItemClickEvent<T> event) {
        DataProvider<T, ?> dataProvider = getGrid().getDataProvider();
        if (!isBuffered() && edited != null && !dataProvider.getId(edited)
                .equals(dataProvider.getId(event.getItem()))) {
            close();
        }
    }

    private void validate(T item) {
        if (getBinder() == null) {
            throw new IllegalStateException(
                    "Editor doesn't have a binder. It's needed to be set explicitly. "
                            + "An example of setting the Binder: "
                            + "Binder<Person> binder = new Binder<>(Person.class); grid.setBinder(binder)");
        }

        if (isBuffered() && edited != null) {
            throw new IllegalStateException("Editing item " + item
                    + " failed. Item editor is already editing item " + edited);
        }

        if (!getGrid().getDataCommunicator().getKeyMapper().has(item)) {
            throw new IllegalStateException("The item " + item
                    + " is not in the backing data provider");
        }
    }

    @Override
    public Registration addSaveListener(EditorSaveListener<T> listener) {
        return addListener(EditorSaveListener.class, listener);
    }

    @Override
    public Registration addCancelListener(EditorCancelListener<T> listener) {
        return addListener(EditorCancelListener.class, listener);
    }

    @Override
    public Registration addOpenListener(EditorOpenListener<T> listener) {
        return addListener(EditorOpenListener.class, listener);
    }

    @Override
    public Registration addCloseListener(EditorCloseListener<T> listener) {
        return addListener(EditorCloseListener.class, listener);
    }

    private <L> Registration addListener(Class<L> listenerType, L listener) {
        @SuppressWarnings("unchecked")
        List<L> list = (List<L>) listeners.computeIfAbsent(listenerType,
                key -> Collections.synchronizedList(new ArrayList<>(1)));
        list.add(listener);
        return () -> list.remove(listener);
    }

    @SuppressWarnings("unchecked")
    private void fireOpenEvent(EditorOpenEvent<T> event) {
        List<EditorOpenListener<T>> list = (List<EditorOpenListener<T>>) listeners
                .get(EditorOpenListener.class);
        if (list == null || list.isEmpty()) {
            return;
        }
        new ArrayList<>(list).forEach(listener -> listener.onEditorOpen(event));
    }

    @SuppressWarnings("unchecked")
    private void fireCancelEvent(EditorCancelEvent<T> event) {
        List<EditorCancelListener<T>> list = (List<EditorCancelListener<T>>) listeners
                .get(EditorCancelListener.class);
        if (list == null || list.isEmpty()) {
            return;
        }
        new ArrayList<>(list)
                .forEach(listener -> listener.onEditorCancel(event));
    }

    @SuppressWarnings("unchecked")
    private void fireSaveEvent(EditorSaveEvent<T> event) {
        List<EditorSaveListener<T>> list = (List<EditorSaveListener<T>>) listeners
                .get(EditorSaveListener.class);
        if (list == null || list.isEmpty()) {
            return;
        }
        new ArrayList<>(list).forEach(listener -> listener.onEditorSave(event));
    }

    @SuppressWarnings("unchecked")
    private void fireCloseEvent(EditorCloseEvent<T> event) {
        List<EditorCloseListener<T>> list = (List<EditorCloseListener<T>>) listeners
                .get(EditorCloseListener.class);
        if (list == null || list.isEmpty()) {
            return;
        }
        new ArrayList<>(list)
                .forEach(listener -> listener.onEditorClose(event));
    }

}
