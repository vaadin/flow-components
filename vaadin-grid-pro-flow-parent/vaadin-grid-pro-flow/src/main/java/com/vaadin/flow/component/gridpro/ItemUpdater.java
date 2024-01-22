package com.vaadin.flow.component.gridpro;

/*
 * #%L
 * Vaadin GridPro
 * %%
 * Copyright (C) 2018 - 2019 Vaadin Ltd
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

import com.vaadin.flow.function.SerializableBiConsumer;

/**
 * Callback that is called when a new value has been entered to an editor.
 *
 *
 * @param <T>
 *            the item type
 * @param <V>
 *            the value type
 *
 */
public interface ItemUpdater<T, V> extends SerializableBiConsumer<T, V> {

    /**
     * Called when a new value has been entered to an editor for an item.
     *
     * @param item
     *            the instance of the item
     * @param newValue
     *            the new value of the property
     */
    @Override
    void accept(T item, V newValue);
}
