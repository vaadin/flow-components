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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-loads-items")
public class GridLoadsItemsPage extends Div {
    public GridLoadsItemsPage() {
        Grid<String> grid = new Grid<>();
        grid.setId("data-grid");
        grid.setPageSize(100);

        VerticalLayout messages = new VerticalLayout();
        messages.setId("messages");

        DataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();

                    String message = "Fetch " + offset + " - "
                            + (offset + limit);
                    messages.add(new Span(message));

                    return IntStream.range(0, 1000).skip(offset).limit(limit)
                            .mapToObj(Integer::toString);
                }, query -> 1000);

        grid.setDataProvider(dataProvider);

        grid.addColumn(item -> item).setHeader("Data");

        NativeButton clearButton = new NativeButton("Clear message",
                e -> messages.removeAll());
        clearButton.setId("clear-messages");

        add(grid, clearButton, messages);
    }
}
