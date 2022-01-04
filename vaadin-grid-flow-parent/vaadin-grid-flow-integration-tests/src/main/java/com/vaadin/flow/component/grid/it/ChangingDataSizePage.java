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
package com.vaadin.flow.component.grid.it;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/changing-data-size")
public class ChangingDataSizePage extends Div {

    private List<String> items;
    private NativeButton removeItems;

    public ChangingDataSizePage() {
        Grid<String> grid = new Grid<>();
        grid.setId("grid");
        grid.addColumn(s -> s);

        items = IntStream.range(0, 133).mapToObj(idx -> "Item " + idx)
                .collect(Collectors.toList());

        removeItems = new NativeButton("Remove 10 items from the DataProvider",
                event -> {
                    for (int i = 0; i < 10; i++) {
                        items.remove(items.size() - 1);
                    }
                });
        removeItems.setId("remove-items");

        CallbackDataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(this::getItems, this::count);
        grid.setItems(dataProvider);
        add(grid, removeItems);
    }

    private Stream<String> getItems(Query<String, Void> query) {
        List<String> subList = items.subList(query.getOffset(),
                query.getOffset() + Math.min(items.size() - query.getOffset(),
                        query.getLimit()));
        return subList.stream();
    }

    private int count(Query<String, Void> query) {
        return items.size();
    }

}
