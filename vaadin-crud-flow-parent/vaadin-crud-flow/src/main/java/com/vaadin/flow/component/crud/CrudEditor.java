/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.crud;

import java.io.Serializable;

import com.vaadin.flow.component.Component;

/**
 * Interface representing a crud editor.
 *
 * @param <E>
 *            the bean type
 */
public interface CrudEditor<E> extends Serializable {

    /**
     * Sets an item to be edited. This could be a newly instantiated item or an
     * existing item from the grid. Initial validation will be skipped.
     *
     * @param item
     *            the item to edit
     * @see #setItem(Object, boolean)
     */
    default void setItem(E item) {
        setItem(item, false);
    }

    /**
     * Sets an item to be edited. This could be a newly instantiated item or an
     * existing item from the grid.
     *
     * @param item
     *            the item to edit
     * @param validate
     *            if true the item will be validated immediately
     */
    void setItem(E item, boolean validate);

    /**
     * Returns the item being edited.
     *
     * @return the item being edited
     */
    E getItem();

    /**
     * Clears the editor.
     */
    void clear();

    /**
     * Runs validations on the data entered into an editor and returns their
     * validity but could also have side-effects such as showing visual
     * indicators for invalid fields.
     *
     * @return true if valid or false if otherwise
     */
    boolean validate();

    /**
     * Writes any pending input update (if any) to the item.
     */
    void writeItemChanges();

    /**
     * Returns the user interface of an editor.
     *
     * @return the user interface
     */
    Component getView();
}
