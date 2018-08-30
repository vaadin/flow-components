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
import com.vaadin.flow.data.binder.HasValidator;

public abstract class CrudEditor<E> extends Component implements HasValidator<E> {

    protected E item;

    public E getItem() {
        return item;
    }

    public void setItem(E item) {
        this.item = item;
    }

    public abstract boolean isValid();

    public abstract boolean isDirty();
}
