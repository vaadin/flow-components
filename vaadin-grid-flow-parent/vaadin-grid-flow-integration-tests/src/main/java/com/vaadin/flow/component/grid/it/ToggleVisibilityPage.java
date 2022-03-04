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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

/**
 * Page that reproduces the bug described at
 * https://github.com/vaadin/flow/issues/4448
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-grid/toggle-visibility")
public class ToggleVisibilityPage extends Div {

    public ToggleVisibilityPage() {
        Grid<String> grid1 = new Grid<>();
        grid1.setItems(
                IntStream.range(0, 100).mapToObj(i -> "Grid1 Item " + i));
        grid1.addColumn(ValueProvider.identity());

        Grid<String> grid2 = new Grid<>();
        grid2.setItems(
                IntStream.range(0, 100).mapToObj(i -> "Grid2 Item " + i));
        grid2.addColumn(ValueProvider.identity());

        Div parent1 = new Div(grid1);
        Div parent2 = new Div(grid2);

        parent2.setVisible(false);

        NativeButton toggleVisibility = new NativeButton("Toggle visibility",
                event -> {
                    parent1.setVisible(!parent1.isVisible());
                    parent2.setVisible(!parent2.isVisible());
                });

        grid1.setId("toggle-visibility-grid1");
        grid2.setId("toggle-visibility-grid2");
        toggleVisibility.setId("toggle-visibility-button");

        add(parent1, parent2, toggleVisibility);
    }

}
