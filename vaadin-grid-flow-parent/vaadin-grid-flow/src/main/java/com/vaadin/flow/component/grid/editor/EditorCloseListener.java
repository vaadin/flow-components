/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.editor;

import java.io.Serializable;

import com.vaadin.flow.component.grid.Grid;

/**
 * An event listener for a {@link Grid} editor close events.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the bean type
 *
 * @see EditorCloseEvent
 * @see Editor#addCloseListener(EditorCloseListener)
 */
@FunctionalInterface
public interface EditorCloseListener<T> extends Serializable {

    /**
     * Called when the editor is closed.
     *
     * @param event
     *            close event
     */
    public void onEditorClose(EditorCloseEvent<T> event);
}
