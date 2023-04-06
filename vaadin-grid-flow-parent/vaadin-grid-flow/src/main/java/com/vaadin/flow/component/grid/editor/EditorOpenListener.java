/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.editor;

import java.io.Serializable;

import com.vaadin.flow.component.grid.Grid;

/**
 * An event listener for a {@link Grid} editor open events.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the bean type
 *
 * @see EditorOpenEvent
 * @see Editor#addOpenListener(EditorOpenListener)
 */
@FunctionalInterface
public interface EditorOpenListener<T> extends Serializable {

    /**
     * Called when the editor is opened.
     *
     * @param event
     *            open event
     */
    public void onEditorOpen(EditorOpenEvent<T> event);
}
