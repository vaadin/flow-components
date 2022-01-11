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

import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

/**
 * Test view that uses Grid with different pageSize settings.
 */
@Route("vaadin-grid/grid-page-size")
public class GridPageSizePage extends Div {

    private DataProvider<String, ?> dataProvider = DataProvider.fromCallbacks(
            query -> getStream(query).mapToObj(Integer::toString),
            query -> 10000);

    private Label info;

    /**
     * Creates a view with a grid with page size of 10.
     */
    public GridPageSizePage() {
        Grid<String> grid = new Grid<>(10);

        grid.setDataProvider(dataProvider);
        grid.addColumn(i -> i).setHeader("text");
        grid.addColumn(i -> String.valueOf(i.length())).setHeader("length");

        info = new Label();
        info.setId("query-info");

        Input size = new Input();
        size.setId("size-input");
        NativeButton button = new NativeButton("Change page size", event -> {
            int pageSize = Integer.parseInt(size.getValue());
            grid.setPageSize(pageSize);
        });
        button.setId("size-submit");

        add(grid, info, new Div(size, button));
    }

    private IntStream getStream(Query<String, Void> query) {
        info.setText(String.format("Query offset: %d Query limit: %d",
                query.getOffset(), query.getLimit()));
        return IntStream.range(query.getOffset(),
                query.getOffset() + query.getLimit());
    }

}
