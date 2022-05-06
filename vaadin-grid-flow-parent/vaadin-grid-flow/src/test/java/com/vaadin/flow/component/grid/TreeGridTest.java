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

package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.function.ValueProvider;

public class TreeGridTest {

    @Test
    public void defaultUniqueKeyProvider_usesIncrementalLongId() {
        Item item1 = new Item("sensitive data 1");
        Item item2 = new Item("sensitive data 2");

        UniqueKeyTreeGrid grid = new UniqueKeyTreeGrid();
        String key1 = grid.getUniqueKeyProvider().apply(item1);
        Assert.assertEquals("0", key1);

        String key2 = grid.getUniqueKeyProvider().apply(item2);
        Assert.assertEquals("1", key2);

        key1 = grid.getUniqueKeyProvider().apply(item1);
        Assert.assertEquals("0", key1);
    }

    private static class UniqueKeyTreeGrid extends TreeGrid<Item> {
        @Override
        public ValueProvider<Item, String> getUniqueKeyProvider() {
            return super.getUniqueKeyProvider();
        }
    }

    private static class Item {
        private final String sensitiveData;

        public Item(String sensitiveData) {
            this.sensitiveData = sensitiveData;
        }

        public String getSensitiveData() {
            return sensitiveData;
        }
    }

}
