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

import java.time.LocalDate;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/scroll-over-100k")
public class GridTestScrollingOver100kLines extends Div {

    public GridTestScrollingOver100kLines() {
        setSizeFull();

        Grid<String> grid = new Grid<>();
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 1");
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 2");
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 3");
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 4");
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 5");
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 6");
        grid.addColumn(value -> LocalDate.now()).setHeader("date");

        grid.setWidth("100%");
        grid.setHeight("300px");

        grid.setItems(
                IntStream.rangeClosed(1, 100500).mapToObj(String::valueOf));
        add(grid);
    }

}
