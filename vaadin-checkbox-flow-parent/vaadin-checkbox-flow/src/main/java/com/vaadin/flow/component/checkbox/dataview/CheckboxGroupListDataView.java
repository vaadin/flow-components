/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.component.checkbox.dataview;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataController;

import java.util.List;

/**
 * {@link CheckboxGroup} component list data view implementation which takes into
 * account the component-specific and common list data API.
 *
 * @param <T>
 *        data type
 */
public class CheckboxGroupListDataView<T> extends AbstractListDataView<T> implements CheckboxGroupDataView<T> {

    public CheckboxGroupListDataView(DataController<T> dataController) {
        super(dataController);
    }

    @Override
    public T getItemOnIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Expected zero or greater index, but was given: " + index);
        }

        List<T> allItems = getAllItemsAsList();
        if (allItems.isEmpty()) {
            throw new IndexOutOfBoundsException("Item requested on an empty data set");
        }
        return allItems.get(index);
    }
}
