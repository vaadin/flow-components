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
import com.vaadin.flow.data.provider.DataView;

/**
 * {@link CheckboxGroup} component {@link DataView} interface for getting
 * specific information on current data set of a {@link CheckboxGroup} instance.
 *
 * @param <T> data type
 */
public interface CheckboxGroupDataView<T> extends DataView<T> {

    /**
     * Get the item at the given index in the sorted and filtered data set.
     *
     * @param index index number
     * @return item on index
     * @throws IndexOutOfBoundsException requested index is outside of the available data set.
     */
    T getItemOnIndex(int index);
}
