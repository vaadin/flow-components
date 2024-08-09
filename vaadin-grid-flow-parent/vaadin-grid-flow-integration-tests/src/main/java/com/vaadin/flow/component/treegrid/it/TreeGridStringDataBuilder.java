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
package com.vaadin.flow.component.treegrid.it;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.data.provider.hierarchy.TreeData;

public class TreeGridStringDataBuilder {

    private final TreeData<String> data = new TreeData<>();

    private final Map<String, String> parentPathMap = new HashMap<>();

    private Map<String, Integer> nameToCountMap = new LinkedHashMap<>();

    public TreeGridStringDataBuilder addLevel(String name, int numberOfItems) {
        nameToCountMap.put(name, numberOfItems);
        return this;
    }

    public TreeData<String> build() {
        List<String> itemsForLevel = null;
        for (Map.Entry<String, Integer> nameToCountMap : nameToCountMap
                .entrySet()) {
            itemsForLevel = addItemsForLevel(nameToCountMap.getKey(),
                    nameToCountMap.getValue(), itemsForLevel);
        }
        return data;
    }

    private List<String> addItemsForLevel(String name, int numberOfItems,
            List<String> parentItems) {
        if (parentItems == null) {
            return addItemsToParent(name, numberOfItems, null);
        }
        return parentItems.stream()
                .map(parent -> addItemsToParent(name, numberOfItems, parent))
                .flatMap(List::stream).collect(Collectors.toList());
    }

    private List<String> addItemsToParent(String name, int numberOfItems,
            String parent) {
        return IntStream.range(0, numberOfItems)
                .mapToObj(index -> createItem(name, parent, index))
                .collect(Collectors.toList());
    }

    private String createItem(String name, String parent, int index) {
        String thisPath = Optional.ofNullable(parentPathMap.get(parent))
                .map(path -> path + "/" + index).orElse("" + index);
        String item = (name + " " + thisPath).trim();
        parentPathMap.put(item, thisPath);
        data.addItem(parent, item);
        return item;
    }
}
