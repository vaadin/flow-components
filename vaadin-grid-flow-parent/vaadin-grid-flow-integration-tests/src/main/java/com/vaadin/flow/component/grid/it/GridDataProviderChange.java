/*
 * Copyright 2000-2023 Vaadin Ltd.
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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd.
 */
@Route("vaadin-grid/grid-data-provider-change")
public class GridDataProviderChange extends Div {

    public GridDataProviderChange() {
        final Grid<String> grid = new Grid<>(
                DataProvider.ofItems("Item 1", "Item 2", "Item 3"));
        grid.addColumn(it -> it).setHeader("First column");

        final NativeButton setItemsButton = new NativeButton("Set items");
        setItemsButton.addClickListener(
                e -> grid.setItems("Item 4", "Item 5", "Item 6"));
        setItemsButton.setId("set-items");

        Div dataProviderChange = new Div();
        dataProviderChange.setId("dataProviderChange");
        grid.addDataProviderChangeListener(e -> dataProviderChange
                .setText("dataProvider event from client " + e.isFromClient()));

        add(grid, setItemsButton, dataProviderChange);
    }

}
