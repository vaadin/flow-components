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
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-grid/grid-cell-focus-page")
public class GridCellFocusPage extends Div {

    public static final String ID_GRID = "cell-focus-grid";
    public static final String ID_ITEM_RESULT = "cell-focus-item-result";
    public static final String ID_COLUMN_RESULT = "cell-focus-column-result";
    public static final String ID_SECTION_RESULT = "cell-focus-section-result";

    public static final String KEY_FIRST_COLUMN = "first-column";
    public static final String KEY_SECOND_COLUMN = "second-column";
    public static final String KEY_THIRD_COLUMN = "third-column";

    public static final String NO_ITEM = "-- no item --";
    public static final String NO_COLUMN = "-- no column --";
    public static final String NO_SECTION = "-- no section --";

    public GridCellFocusPage() {
        setSizeFull();

        Grid<String> grid = new Grid<>();
        grid.setId(ID_GRID);

        grid.addColumn(s -> s + "1").setKey(KEY_FIRST_COLUMN)
                .setHeader("First column header")
                .setFooter("First column footer");
        grid.addColumn(s -> s + "2").setKey(KEY_SECOND_COLUMN)
                .setHeader("Second column header")
                .setFooter("Second column footer");
        grid.addColumn(s -> s + "3").setKey(KEY_THIRD_COLUMN)
                .setHeader("Third column header")
                .setFooter("Third column footer");

        grid.setItems("A", "B", "C", "D");

        Span itemResult = new Span();
        itemResult.setId(ID_ITEM_RESULT);

        Span colResult = new Span();
        colResult.setId(ID_COLUMN_RESULT);

        Span sectionResult = new Span();
        sectionResult.setId(ID_SECTION_RESULT);

        add(itemResult, colResult, sectionResult, grid);

        grid.addCellFocusListener(event -> {
            String item = event.getItem().orElse(NO_ITEM);

            String column = event.getColumn().map(Column::getKey)
                    .orElse(NO_COLUMN);

            itemResult.setText(item);
            colResult.setText(column);
            sectionResult.setText(event.getSection().getClientSideName());
        });
    }
}
