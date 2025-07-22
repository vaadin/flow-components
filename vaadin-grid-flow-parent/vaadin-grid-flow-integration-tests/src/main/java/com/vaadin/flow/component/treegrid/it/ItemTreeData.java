/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.treegrid.it;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import com.vaadin.flow.data.provider.hierarchy.TreeData;

public class ItemTreeData extends TreeData<ItemTreeData.Item> {
    public static class Item {
        private String name;
        private Integer id;

        public Item(String name, Integer id) {
            this.name = name;
            this.id = id;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Item item) {
                return this.getId().equals(item.getId());
            }
            return false;
        }
    }

    private static AtomicInteger idGenerator = new AtomicInteger(0);

    public ItemTreeData(int... levelSizes) {
        super();

        addItems(generateItems(null, levelSizes[0]), (parentItem) -> {
            int parentItemDepth = getItemDepth(parentItem);
            if (parentItemDepth < levelSizes.length - 1) {
                return generateItems(parentItem,
                        levelSizes[parentItemDepth + 1]);
            }

            return Collections.emptyList();
        });
    }

    private List<Item> generateItems(Item parentItem, int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> new Item(
                        parentItem != null ? parentItem.getName() + "-" + i
                                : "Item " + i,
                        idGenerator.incrementAndGet()))
                .toList();
    }

    private int getItemDepth(Item item) {
        return item.getName().split("-").length - 1;
    }
}
