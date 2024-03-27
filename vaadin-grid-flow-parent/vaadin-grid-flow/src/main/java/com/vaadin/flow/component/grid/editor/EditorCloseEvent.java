/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.editor;

/**
 * Holds the data for close events fired in {@link Editor}.
 *
 * @author Vaadin Ltd
 *
 * @see EditorCloseListener
 *
 * @param <T>
 *            the item type
 */
public class EditorCloseEvent<T> extends EditorEvent<T> {

    /**
     * Constructor for the editor close event.
     *
     * @param editor
     *            the source of the event
     * @param item
     *            the item being edited
     */
    public EditorCloseEvent(Editor<T> editor, T item) {
        super(editor, item);
    }

}
