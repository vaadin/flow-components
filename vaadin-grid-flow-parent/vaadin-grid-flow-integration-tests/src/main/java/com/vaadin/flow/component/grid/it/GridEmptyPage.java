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

import java.util.UUID;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-empty")
public class GridEmptyPage extends Div {

    public GridEmptyPage() {
        Grid<Person> grid = new Grid<>();
        grid.setId("empty-grid");

        grid.addColumn(Person::getFirstName)
                .setKey(UUID.randomUUID().toString()).setHeader("First name")
                .setSortable(true);

        add(grid);

        final Button clearCache = new Button("Clear cache",
                event -> grid.getElement().executeJs("this.clearCache()"));
        clearCache.setId("clear-cache-button");
        add(clearCache);
    }

}
