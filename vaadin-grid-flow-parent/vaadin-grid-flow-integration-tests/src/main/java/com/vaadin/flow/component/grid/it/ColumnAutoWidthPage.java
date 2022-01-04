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

@Route("vaadin-grid/column-auto-width")
public class ColumnAutoWidthPage extends Div {

    public static final String GRID_ID = "auto-width-grid";

    public ColumnAutoWidthPage() {
        Grid<Person> grid = new Grid<>();
        grid.setId(GRID_ID);
        grid.getStyle().set("--lumo-font-family",
                "Arial, Helvetica, sans-serif");
        grid.setItems(new Person("Jorma", 2018));

        grid.addComponentColumn(person -> {
            NativeButton button = new NativeButton("F");
            return button;
        }).setAutoWidth(true).setHeader("A").setFlexGrow(0);
        grid.addColumn(Person::getId).setHeader("B");
        grid.addColumn(
                person -> "This is some length of text to check auto column width.")
                .setAutoWidth(true).setHeader("C").setFlexGrow(0);
        grid.addColumn(person -> "-").setAutoWidth(true)
                .setHeader("Some long text for column header").setFlexGrow(0);

        add(grid);
    }

}
