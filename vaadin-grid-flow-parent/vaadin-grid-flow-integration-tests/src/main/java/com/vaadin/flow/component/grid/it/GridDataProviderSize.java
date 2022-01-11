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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Vaadin Ltd.
 */
@Route("vaadin-grid/grid-data-provider-size")
public class GridDataProviderSize extends Div {

    public GridDataProviderSize() {
        final Grid<String> grid = new Grid<>();
        final Grid.Column<String> col = grid.addColumn(it -> it)
                .setHeader("First column");

        Div div = new Div();
        div.setText("0");
        div.setId("info");

        grid.setDataProvider(new AbstractBackEndDataProvider<String, Object>() {
            @Override
            protected Stream<String> fetchFromBackEnd(
                    Query<String, Object> query) {
                return Stream.of();
            }

            @Override
            protected int sizeInBackEnd(Query<String, Object> query) {
                Integer count = Integer.parseInt(div.getText()) + 1;
                div.setText(count.toString());
                return 0;
            }
        });

        grid.sort(Arrays
                .asList(new GridSortOrder<>(col, SortDirection.ASCENDING)));
        add(grid, div);
    }

}
