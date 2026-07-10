/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1675.
 *
 * A full-width filter TextField set as the header of a sortable column
 * reportedly does not stretch to the column width, because the header content
 * of a sortable column is wrapped in a content-sized vaadin-grid-sorter.
 */
@Route("repro-1675")
public class Repro1675View extends Div {

    public Repro1675View() {
        Grid<String> grid = new Grid<>();
        grid.setItems("one", "two");

        TextField sortableFilter = new TextField();
        sortableFilter.setPlaceholder("filter (sortable column)");
        sortableFilter.setWidth("100%");
        sortableFilter.setId("sortable-filter");
        grid.addColumn(item -> item).setHeader(sortableFilter)
                .setSortable(true);

        TextField plainFilter = new TextField();
        plainFilter.setPlaceholder("filter (plain column)");
        plainFilter.setWidth("100%");
        plainFilter.setId("plain-filter");
        grid.addColumn(item -> item).setHeader(plainFilter);

        grid.setWidth("900px");
        add(grid);
    }
}
