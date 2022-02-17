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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-on-flex-layout")
public class GridOnFlexLayoutPage extends FlexLayout {

    public GridOnFlexLayoutPage() {
        setSizeFull();

        Grid<String> grid = new Grid<>();
        grid.setId("full-size-grid");
        grid.setItems(Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.addColumn(ValueProvider.identity());
        grid.setSizeFull();

        add(grid);
    }

}
