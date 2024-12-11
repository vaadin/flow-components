/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.virtuallist;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.shared.Registration;

/**
 * Selection model implementation for disabling selection in VirtualList.
 *
 * @param <T>
 *            the virtual list bean type
 */
public class VirtualListNoneSelectionModel<T>
        implements SelectionModel<VirtualList<T>, T> {

    @Override
    public Set<T> getSelectedItems() {
        return Collections.emptySet();
    }

    @Override
    public Optional<T> getFirstSelectedItem() {
        return Optional.empty();
    }

    @Override
    public void select(T item) {
        // NO-OP
    }

    @Override
    public void deselect(T item) {
        // NO-OP
    }

    @Override
    public void deselectAll() {
        // NO-OP
    }

    @Override
    public Registration addSelectionListener(
            SelectionListener<VirtualList<T>, T> listener) {
        throw new UnsupportedOperationException(
                "This selection model doesn't allow selection, cannot add selection listeners to it. "
                        + "Please set suitable selection mode with virtualList.setSelectionMode");
    }

}
