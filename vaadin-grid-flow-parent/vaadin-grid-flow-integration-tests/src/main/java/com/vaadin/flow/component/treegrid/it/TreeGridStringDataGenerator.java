package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.data.provider.hierarchy.TreeData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreeGridStringDataGenerator {

    private final TreeData<String> data = new TreeData<>();

    private final Map<String, String> parentPathMap = new HashMap<>();

    private Map<Integer, String> levelToNameMap;

    public TreeGridStringDataGenerator() {
        this(Map.of(0, "Granddad", 1, "Dad", 2, "Son"));
    }

    public TreeGridStringDataGenerator(Map<Integer, String> levelToNameMap) {
        this.levelToNameMap = levelToNameMap;
    }

    public TreeData<String> generate(int... countPerLevel) {
        List<String> itemsForLevel = null;
        for (int level = 0; level < countPerLevel.length; level++) {
            itemsForLevel = addItemsForLevel(levelToNameMap.get(level),
                    countPerLevel[level], itemsForLevel);
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
