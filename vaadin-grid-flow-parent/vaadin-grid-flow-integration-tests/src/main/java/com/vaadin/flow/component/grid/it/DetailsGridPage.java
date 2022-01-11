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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/**
 * Details grid uses the same component for all shown details. If the grid
 * itemDetailsDataGenerator is not correctly cleaned then there will be an
 * exception when navigating and all data is cleared.
 */
@Route("vaadin-grid/detailsGrid")
public class DetailsGridPage extends Div {

    Span text = new Span("wow");

    public DetailsGridPage() {
        Grid<Person> grid = new Grid<>(Person.class);

        grid.setItems(new Person("Jorma", 2018), new Person("Jarmo", 2018),
                new Person("Jethro", 2018));

        grid.setItemDetailsRenderer(new ComponentRenderer<>(person -> {
            text.setText(person.getFirstName());
            return text;
        }));

        RouterLink next = new RouterLink("next", DisabledGridPage.class);
        next.setId("next");
        add(grid, next);
    }
}
