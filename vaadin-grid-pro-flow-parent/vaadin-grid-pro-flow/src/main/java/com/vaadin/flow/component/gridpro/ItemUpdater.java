/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.gridpro;

/*
 * #%L
 * Vaadin GridPro
 * %%
 * Copyright (C) 2018 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
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
