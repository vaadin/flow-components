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

import java.io.Serializable;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

/**
 * An editor in a Grid.
 * <p>
 * This class contains methods for editor functionality: configure an editor
 * {@link Binder}, open the editor, save and cancel a row editing.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 */
public interface Editor<T> extends Serializable {

    /**
     * Sets the underlying Binder to this Editor.
     *
     * @param binder
     *            the binder for updating editor fields; not {@code null}
     * @return this editor
     */
    public Editor<T> setBinder(Binder<T> binder);

    /**
     * Returns the underlying Binder from Editor.
     *
     * @return the binder; not {@code null}
     */
    public Binder<T> getBinder();

    /**
     * Sets the Editor buffered mode. When the editor is in buffered mode, edits
     * are only committed when the user clicks the save button. In unbuffered
     * mode valid changes are automatically committed.
     *
     * @param buffered
     *            {@code true} if editor should be buffered; {@code false} if
     *            not
     * @return this editor
     */
    public Editor<T> setBuffered(boolean buffered);

    /**
     * Returns whether Editor is buffered or not.
     *
     * @see #setBuffered(boolean)
     *
     * @return {@code true} if editor is buffered; {@code false} if not
     */
    public boolean isBuffered();

    /**
     * Returns whether Editor is open or not.
     *
     * @return {@code true} if editor is open; {@code false} if not
     */
    public boolean isOpen();

    /**
     * Saves any changes from the Editor fields to the edited bean.
     *
     * @return {@code true} if save succeeded; {@code false} if not
     */
    public boolean save();

    /**
     * Close the editor discarding any unsaved changes.
     */
    public void cancel();

    /**
     * Opens the editor interface for the provided item.
     *
     * @param item
     *            the edited item
     * @throws IllegalStateException
     *             if already editing a different item in buffered mode
     * @throws IllegalArgumentException
     *             if the {@code item} is not in the backing data provider
     */
    public void editItem(T item);

    /**
     * Gets the Grid instance which this editor belongs to.
     *
     * @return the grid which owns the editor
     */
    public Grid<T> getGrid();

    /**
     * Adds an editor save {@code listener}.
     *
     * @param listener
     *            save listener
     * @return a registration object for removing the listener
     */
    public Registration addSaveListener(EditorSaveListener<T> listener);

    /**
     * Adds an editor cancel {@code listener}.
     *
     * @param listener
     *            cancel listener
     * @return a registration object for removing the listener
     */
    public Registration addCancelListener(EditorCancelListener<T> listener);

    /**
     * Adds an editor open {@code listener}.
     *
     * @param listener
     *            open listener
     * @return a registration object for removing the listener
     *
     */
    public Registration addOpenListener(EditorOpenListener<T> listener);
}
