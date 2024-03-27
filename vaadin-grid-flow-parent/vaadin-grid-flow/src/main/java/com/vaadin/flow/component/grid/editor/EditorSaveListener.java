/**
 * Copyright (C) 2000-2024 Vaadin Ltd
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
 * An event listener for a {@link Grid} editor save events.
 *
 * @author Vaadin Ltd
 *
 * @see EditorSaveEvent
 * @see Editor#addSaveListener(EditorSaveListener)
 */
@FunctionalInterface
public interface EditorSaveListener<T> extends Serializable {

    /**
     * Called when the editor is saved.
     *
     * @param event
     *            save event
     */
    public void onEditorSave(EditorSaveEvent<T> event);
}
