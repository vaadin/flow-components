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
 * An event listener for a {@link Grid} editor cancel events.
 *
 * @author Vaadin Ltd
 *
 * @see EditorCancelEvent
 * @see Editor#addCancelListener(EditorCancelListener)
 *
 * @param <T>
 *            the bean type
 */
@FunctionalInterface
public interface EditorCancelListener<T> extends Serializable {

    /**
     * Called when the editor is cancelled.
     *
     * @param event
     *            cancel event
     */
    public void onEditorCancel(EditorCancelEvent<T> event);
}
