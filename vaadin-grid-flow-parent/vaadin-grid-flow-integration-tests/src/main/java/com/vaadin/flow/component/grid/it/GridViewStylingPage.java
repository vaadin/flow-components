/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
