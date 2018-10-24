package com.vaadin.flow.component.crud;

/*
 * #%L
 * Vaadin Crud for Vaadin 10
 * %%
 * Copyright (C) 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

/**
 * A CRUD editor that binds editor fields to bean properties using a {@link Binder}.
 *
 * @param <E> the bean type
 * @see Binder
 */
public class BinderCrudEditor<E> implements CrudEditor<E> {

    private final Binder<E> binder;
    private final Component view;
    private E item;

    /**
     * Initializes a BinderCrudEditor with the given binder and no form view
     *
     * @param binder the editor binder
     */
    public BinderCrudEditor(Binder<E> binder) {
        this(binder, null);
    }

    /**
     * Initializes a BinderCrudEditor with the given binder and form view
     *
     * @param binder the editor binder
     * @param view the form view
     */
    public BinderCrudEditor(Binder<E> binder, Component view) {
        this.binder = binder;
        this.view = view;
    }

    @Override
    public void setItem(E item, boolean validate) {
        this.item = item;
        binder.readBean(item);
        if (validate) {
            binder.validate();
        }
    }

    @Override
    public E getItem() {
        return item;
    }

    @Override
    public void writeItemChanges() {
        try {
            binder.writeBean(item);
        } catch (ValidationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Frees the item, and lazily clears all input fields.
     */
    @Override
    public void clear() {
        this.item = null;
        binder.readBean(null);
        binder.getFields().forEach(HasValue::clear);
    }

    @Override
    public boolean isValid() {
        return binder.isValid();
    }

    @Override
    public Component getView() {
        return view;
    }
}
