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
import com.vaadin.flow.component.shared.TooltipConfiguration;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/tooltip")
public class GridTooltipPage extends Div {

    public GridTooltipPage() {
        // Reset default delay values from 500 to 0
        TooltipConfiguration.setDefaultFocusDelay(0);
        TooltipConfiguration.setDefaultHoverDelay(0);
        TooltipConfiguration.setDefaultHideDelay(0);

        var grid = new Grid<>(Person.class);
        grid.setItems(new Person("Jack", 32), new Person("Jill", 33));

        grid.getColumnByKey("firstName")
                .setTooltipGenerator(person -> "First name of the person is "
                        + person.getFirstName());

        var setGridTooltipButton = new NativeButton(
                "Set tooltip to all columns", clickEvent -> {
                    grid.setTooltipGenerator(person -> "Grid's tooltip! "
                            + person.getFirstName());
                });
        setGridTooltipButton.setId("set-grid-tooltip-button");

        var addColumnButton = new NativeButton("Add extra column",
                clickEvent -> {
                    grid.addColumn(item -> "Extra column");
                });
        addColumnButton.setId("add-column-button");

        var setAgeTooltipButton = new NativeButton("Set tooltip to age column",
                event -> {
                    grid.getColumnByKey("age").setTooltipGenerator(
                            person -> "Age of the person is "
                                    + person.getAge());
                });
        setAgeTooltipButton.setId("set-age-tooltip-button");

        var toggleGridButton = new NativeButton("Toggle grid", event -> {

            if (grid.getParent().isPresent()) {
                remove(grid);
            } else {
                add(grid);
            }
        });
        toggleGridButton.setId("toggle-grid-button");

        grid.setId("grid-with-tooltips");
        add(setGridTooltipButton, addColumnButton, setAgeTooltipButton,
                toggleGridButton, grid);
    }

}
