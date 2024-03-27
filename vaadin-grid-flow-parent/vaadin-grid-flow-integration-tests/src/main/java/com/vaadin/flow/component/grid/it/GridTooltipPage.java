/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
