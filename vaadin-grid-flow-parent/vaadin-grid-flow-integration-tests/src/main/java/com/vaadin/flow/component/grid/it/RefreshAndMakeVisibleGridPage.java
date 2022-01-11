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

import java.util.ArrayList;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/refresh-invisible-grid")
public class RefreshAndMakeVisibleGridPage extends Div {

    private Grid<String> grid;
    private ListDataProvider<String> dataProvider;

    public RefreshAndMakeVisibleGridPage() {
        grid = new Grid<>();
        dataProvider = new ListDataProvider<>(new ArrayList<>());
        grid.setDataProvider(dataProvider);
        grid.setVisible(false);
        grid.addColumn(ValueProvider.identity()).setHeader("Name");

        NativeButton button = new NativeButton("Make grid visible", event -> {
            dataProvider.getItems().clear();
            dataProvider.getItems().add("foo");
            dataProvider.refreshAll();
            grid.setVisible(true);
        });

        button.setId("refresh");

        add(grid, button);
    }

}
