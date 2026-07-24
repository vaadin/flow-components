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

import java.util.List;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/web-components/issues/8023.
 *
 * Grid with all rows visible, a component column containing a VerticalLayout
 * (taller rows than default), and a prepended footer row: the initial grid
 * height is too small and the footer appears in the wrong place until the grid
 * is scrolled.
 */
@Route("repro-8023")
public class Repro8023View extends Div {

    public Repro8023View() {
        List<String> items = IntStream.rangeClosed(1, 15)
                .mapToObj(String::valueOf).toList();

        // Failing case: component column with a padded VerticalLayout
        Grid<String> grid = new Grid<>();
        grid.setId("grid");
        Grid.Column<String> str = grid.addColumn(s -> s).setHeader("STR");
        grid.addComponentColumn(s -> {
            VerticalLayout cellLayout = new VerticalLayout();
            cellLayout.add(new Span("ORG1A"));
            return cellLayout;
        });
        grid.setItems(items);
        grid.setAllRowsVisible(true);
        FooterRow footerRow = grid.prependFooterRow();
        footerRow.getCell(str).setComponent(new Paragraph("It works!"));

        // Control: same setup but plain text columns (default row height)
        Grid<String> controlGrid = new Grid<>();
        controlGrid.setId("control-grid");
        Grid.Column<String> controlStr = controlGrid.addColumn(s -> s)
                .setHeader("STR");
        controlGrid.addColumn(s -> "ORG1A").setHeader("ORG");
        controlGrid.setItems(items);
        controlGrid.setAllRowsVisible(true);
        FooterRow controlFooterRow = controlGrid.prependFooterRow();
        controlFooterRow.getCell(controlStr)
                .setComponent(new Paragraph("It works!"));

        add(new H3("Component column (issue #8023)"), grid,
                new H3("Control: text columns"), controlGrid);
    }
}
