/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.virtuallist.tests;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.tests.AbstractSignalsJUnit6Test;

class VirtualListSignalTest extends AbstractSignalsJUnit6Test {

    @Test
    void bindItems_thenSetDataProvider_throws() {
        var list = createVirtualListWithBoundItems();
        Assertions.assertThrows(BindingActiveException.class,
                () -> list.setDataProvider(DataProvider.ofItems("New Item 1", "New Item 2")));
    }

    @Test
    void bindItems_thenSetCollection_throws() {
        var list = createVirtualListWithBoundItems();
        Assertions.assertThrows(BindingActiveException.class,
                () -> list.setItems(Collections.emptyList()));
    }

    @Test
    void bindItems_thenSetItems_throws() {
        var list = createVirtualListWithBoundItems();
        Assertions.assertThrows(BindingActiveException.class,
                () -> list.setItems("New Item 1"));
    }

    @Test
    void bindItems_thenSetStream_throws() {
        var list = createVirtualListWithBoundItems();
        Assertions.assertThrows(BindingActiveException.class,
                () -> list.setItems(Stream.of("New Item 1", "New Item 2")));
    }

    private VirtualList<String> createVirtualListWithBoundItems() {
        var list = new VirtualList<String>();
        var itemsSignal = new ListSignal<String>();
        itemsSignal.insertLast("Item 1");
        itemsSignal.insertLast("Item 2");
        list.bindItems(itemsSignal);
        ui.add(list);
        return list;
    }
}
