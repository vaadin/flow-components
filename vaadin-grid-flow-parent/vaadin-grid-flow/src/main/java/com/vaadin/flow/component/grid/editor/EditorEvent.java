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

import java.util.EventObject;

import com.vaadin.flow.component.grid.Grid;

/**
 * Base class for events fired in {@link Editor}.
 *
 * @author Vaadin Ltd
 *
 * @see EditorOpenListener
 * @see EditorCancelListener
 * @see EditorCloseListener
 * @see EditorSaveListener
 *
 * @param <T>
 *            the item type
 */
public abstract class EditorEvent<T> extends EventObject {

    private T item;

    /**
     * Constructor for the editor event.
     *
     * @param editor
     *            the source of the event
     * @param item
     *            the item being edited
     */
    public EditorEvent(Editor<T> editor, T item) {
        super(editor);
        this.item = item;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Editor<T> getSource() {
        return (Editor<T>) super.getSource();
    }

    /**
     * Gets the editors' grid.
     *
     * @return the editors' grid
     */
    public Grid<T> getGrid() {
        return getSource().getGrid();
    }

    /**
     * Gets the item being edited.
     *
     * @return the item being edited
     */
    public T getItem() {
        return item;
    }
}
