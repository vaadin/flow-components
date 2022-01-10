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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/column-path")
public class ColumnPathPage extends Div {

    public ColumnPathPage() {
        Grid<Person> grid = new Grid<>();

        grid.setItems(new Person("Person 1", null, null, 42, null, null),
                new Person("Person 2", null, null, 42, null, null));

        grid.addColumn(Person::getFirstName).setHeader("Using path");
        grid.addColumn(TemplateRenderer.<Person> of("[[item.firstName]]")
                .withProperty("firstName", Person::getFirstName))
                .setHeader("Using template");
        grid.addColumn(Person::getFirstName)
                .setHeader("Using template because of editor")
                .setEditorComponent(new TextField());

        add(grid);
    }

}
