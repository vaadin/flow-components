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

import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/gridsetitemsafterdetachpage")
public class GridSetItemsAfterDetachPage extends VerticalLayout {

    public GridSetItemsAfterDetachPage() {
        List<String> items = Arrays.asList("foo", "bar");

        Grid<String> grid = new Grid<>();
        grid.setItems(items);
        grid.addColumn(s -> s);

        NativeButton detach = new NativeButton("detach", e -> remove(grid));
        NativeButton setItemsAndAttach = new NativeButton(
                "set items and attach", e -> {
                    grid.setItems(items);
                    add(grid);
                });

        detach.setId("detach");
        setItemsAndAttach.setId("set-items-and-attach");

        add(grid, detach, setItemsAndAttach);
    }

}
