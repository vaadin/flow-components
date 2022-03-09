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

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/styling")
public class GridViewStylingPage extends LegacyTestView {

    public GridViewStylingPage() {
        createStyling();
    }

    private void createStyling() {
        String instructions = "<p>In order to inject styles into Grid cells, "
                + "create a style-module like in the snippet below, "
                + "put it into an html-file in your resources folder, "
                + "and import it with <code>@HtmlImport</code>. "
                + "After this you can apply the CSS classes "
                + "(<code>subscriber</code> and <code>minor</code> in this case) "
                + "into grid rows and cells as shown in the next example.</p>";
        addCard("Styling", "Styling Grid Cells", new Html(instructions));

        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());
        grid.setSelectionMode(SelectionMode.NONE);

        grid.addColumn(Person::getFirstName).setHeader("Name");
        Column<Person> ageColumn = grid.addColumn(Person::getAge)
                .setHeader("Age");
        grid.addColumn(person -> person.isSubscriber() ? "Yes" : "")
                .setHeader("Subscriber");

        grid.setClassNameGenerator(
                person -> person.isSubscriber() ? "subscriber" : "");

        ageColumn.setClassNameGenerator(
                person -> person.getAge() < 18 ? "minor" : "");

        grid.setId("class-name-generator");
        addCard("Styling", "Generating CSS Class Names for Cells", grid);
    }
}
