/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.flow.component.grid;

import java.util.Objects;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.grid.Grid.AbstractGridExtension;
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

    private Binder<T> binder;
    private T edited;
    private boolean isBuffered;

    private static class SaveEvent<T> extends ComponentEvent<Grid<T>> {

        public SaveEvent(Grid<T> source) {
            super(source, false);
        }

    }

    private static class CancelEvent<T> extends ComponentEvent<Grid<T>> {

        public CancelEvent(Grid<T> source) {
            super(source, false);
        }

    }

    private static class EditEvent<T> extends ComponentEvent<Grid<T>> {

        public EditEvent(Grid<T> source) {
            super(source, false);
        }

    }

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
                ComponentUtil.fireEvent(getGrid(), new SaveEvent<T>(getGrid()));
                close();
                return true;
            }
        }
        return false;
    }

    @Override
    public void cancel() {
        ComponentUtil.fireEvent(getGrid(), new CancelEvent<T>(getGrid()));
        close();
    }

    @Override
    public void editItem(T item) {
        Objects.requireNonNull(item, "Editor can't edit null");

        validate(item);

        close();
        edited = item;

        refresh(item);

        if (isBuffered()) {
            binder.readBean(item);
        } else {
            binder.setBean(item);
        }

        ComponentUtil.fireEvent(getGrid(), new EditEvent<T>(getGrid()));
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
            refresh(edited);
            edited = null;
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
                    "Editor doesn't have a binder. It's needed to be set explicitly.");
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Registration addSaveListener(EditorSaveListener<T> listener) {
        ComponentEventListener componentListener = event -> listener
                .onEditorSave(new EditorSaveEvent<T>(this, edited));
        return ComponentUtil.addListener(getGrid(), SaveEvent.class,
                componentListener);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Registration addCancelListener(EditorCancelListener<T> listener) {
        ComponentEventListener componentListener = event -> listener
                .onEditorCancel(new EditorCancelEvent<T>(this, edited));
        return ComponentUtil.addListener(getGrid(), CancelEvent.class,
                componentListener);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Registration addOpenListener(EditorOpenListener<T> listener) {
        ComponentEventListener componentListener = event -> listener
                .onEditorOpen(new EditorOpenEvent<T>(this, edited));
        return ComponentUtil.addListener(getGrid(), EditEvent.class,
                componentListener);
    }

}
