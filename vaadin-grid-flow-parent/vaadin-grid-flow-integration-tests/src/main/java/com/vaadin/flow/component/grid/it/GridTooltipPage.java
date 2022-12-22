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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/tooltip")
public class GridTooltipPage extends Div {

    public GridTooltipPage() {
        var grid = new Grid<>(Person.class);
        grid.setItems(new Person("Jack", 32), new Person("Jill", 33));

        grid.getColumnByKey("firstName")
                .setTooltipGenerator(person -> "First name of the person is "
                        + person.getFirstName());

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

        add(setAgeTooltipButton, toggleGridButton, grid);
    }

}
