/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

/**
 * Test view that adds header rows with components to a Grid.
 */
@Route("vaadin-grid/grid-header-row-with-components")
public class GridHeaderRowWithComponentsPage extends Div {

    public GridHeaderRowWithComponentsPage() {
        var grid = new Grid<String>();
        grid.setItems("Item 1", "Item 2", "Item 3");
        var column1 = grid.addColumn(ValueProvider.identity());
        var column2 = grid.addColumn(ValueProvider.identity());
        add(grid);

        var headerRow1 = grid.appendHeaderRow();
        headerRow1.getCell(column1).setComponent(new NativeLabel("foo"));
        var initiallyHiddenHeader1 = new NativeLabel("Initially hidden foo");
        initiallyHiddenHeader1.setVisible(false);
        headerRow1.getCell(column2).setComponent(initiallyHiddenHeader1);

        var headerRow2 = grid.appendHeaderRow();
        headerRow2.getCell(column1).setComponent(new NativeLabel("bar"));
        var initiallyHiddenHeader2 = new NativeLabel("Initially hidden bar");
        initiallyHiddenHeader2.setVisible(false);
        headerRow2.getCell(column2).setComponent(initiallyHiddenHeader2);

        var button = new NativeButton(
                "Prepend header, set first text and then component");
        button.setId("set-both-text-and-component");
        button.addClickListener(event -> {
            var topRow = grid.prependHeaderRow();
            topRow.getCell(column1).setText("this is text");
            topRow.getCell(column1)
                    .setComponent(new NativeLabel("this is component"));
        });
        add(button);

        var toggleCol2HeadersVisible = new NativeButton(
                "Toggle col2 headers visible");
        toggleCol2HeadersVisible.addClickListener(event -> {
            initiallyHiddenHeader1
                    .setVisible(!initiallyHiddenHeader1.isVisible());
            initiallyHiddenHeader2
                    .setVisible(!initiallyHiddenHeader2.isVisible());
        });
        toggleCol2HeadersVisible.setId("toggle-col-2-headers-visible");
        add(toggleCol2HeadersVisible);
    }
}
