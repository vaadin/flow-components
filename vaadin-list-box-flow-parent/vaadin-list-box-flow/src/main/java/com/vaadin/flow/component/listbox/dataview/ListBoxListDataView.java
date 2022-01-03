/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.listbox.dataview;

import com.vaadin.flow.component.listbox.ListBoxBase;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * Data view implementation for ListBox with in-memory list data. Provides
 * information on the data and allows operations on it.
 *
 * @param <T>
 *            data type
 * @since
 */
public class ListBoxListDataView<T> extends AbstractListDataView<T> {

    /**
     * Creates a new in-memory data view for ListBox and verifies the passed
     * data provider is compatible with this data view implementation.
     *
     * @param dataProviderSupplier
     *            data provider supplier
     * @param listBox
     *            listBox instance for this DataView
     * @param filterOrSortingChangedCallback
     *            callback, which is being invoked when the ListBox's filtering
     *            or sorting changes, not <code>null</code>
     */
    public ListBoxListDataView(
            SerializableSupplier<? extends DataProvider<T, ?>> dataProviderSupplier,
            ListBoxBase listBox,
            SerializableBiConsumer<SerializablePredicate<T>, SerializableComparator<T>> filterOrSortingChangedCallback) {
        super(dataProviderSupplier, listBox, filterOrSortingChangedCallback);
    }
}
