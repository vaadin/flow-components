/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
