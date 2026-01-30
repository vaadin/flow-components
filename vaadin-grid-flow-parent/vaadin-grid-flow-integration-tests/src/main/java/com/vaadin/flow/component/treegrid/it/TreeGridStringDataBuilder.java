/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
