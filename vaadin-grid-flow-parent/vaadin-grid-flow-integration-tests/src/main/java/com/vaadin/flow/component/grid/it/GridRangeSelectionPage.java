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
package com.vaadin.flow.component.grid.it;

import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-range-selection")
public class GridRangeSelectionPage extends Div {

    public GridRangeSelectionPage() {
        Grid<String> grid = new Grid<>(10);

        grid.setItems(
                DataProvider
                        .fromCallbacks(
                                query -> IntStream
                                        .range(query.getOffset(),
                                                query.getOffset()
                                                        + query.getLimit())
                                        .mapToObj(Integer::toString),
                                query -> 10000));
        grid.setSelectionMode(SelectionMode.MULTI);
        add(grid);
    }
}
