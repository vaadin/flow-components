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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd.
 */
@Route("vaadin-grid/grid-virtual-scrolling")
public class GridVirtualScrollingPage extends Div {

    private DataProvider<String, ?> dataProvider = DataProvider.fromCallbacks(
            query -> getStream(query).mapToObj(Integer::toString),
            query -> 100 * 1000 * 1000);

    private Label info;

    public GridVirtualScrollingPage() {
        Grid<String> grid = new Grid<>();

        grid.setDataProvider(dataProvider);
        grid.addColumn(i -> i).setHeader("text");
        grid.addColumn(i -> String.valueOf(i.length())).setHeader("length");

        info = new Label();
        info.setId("query-info");

        add(grid, info);
    }

    private IntStream getStream(Query<String, Void> query) {
        info.setText(String.format("Query offset: %d Query limit: %d",
                query.getOffset(), query.getLimit()));
        return IntStream.range(query.getOffset(),
                query.getOffset() + query.getLimit());
    }

}
