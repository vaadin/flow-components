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
package com.vaadin.flow.component.virtuallist.tests;

import java.util.Set;

import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.internal.JsonUtils;

import elemental.json.Json;
import elemental.json.JsonArray;

public class VirtualListTestHelpers {

    public static <T> boolean generatesSelected(DataGenerator<T> dataGenerator,
            T item) {
        var jsonObject = Json.createObject();
        dataGenerator.generateData(item, jsonObject);
        return jsonObject.hasKey("selected");
    }

    @SuppressWarnings("unchecked")
    public static <T> CompositeDataGenerator<T> getDataGenerator(
            VirtualList<T> list) {
        try {
            var dataGenerator = VirtualList.class
                    .getDeclaredField("dataGenerator");
            dataGenerator.setAccessible(true);
            return (CompositeDataGenerator<T>) dataGenerator.get(list);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> JsonArray getKeysFromItems(VirtualList<T> list,
            Set<T> items) {
        return JsonUtils.listToJson(items.stream().map(
                item -> list.getDataCommunicator().getKeyMapper().key(item))
                .toList());
    }

    public static <T> void updateSelectionFromClient(VirtualList<T> list,
            Set<T> addedItems, Set<T> removedItems) {
        var addedKeys = getKeysFromItems(list, addedItems);
        var removedKeys = getKeysFromItems(list, removedItems);

        try {
            var updateSelection = VirtualList.class.getDeclaredMethod(
                    "updateSelection", JsonArray.class, JsonArray.class);
            updateSelection.setAccessible(true);
            updateSelection.invoke(list, addedKeys, removedKeys);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
