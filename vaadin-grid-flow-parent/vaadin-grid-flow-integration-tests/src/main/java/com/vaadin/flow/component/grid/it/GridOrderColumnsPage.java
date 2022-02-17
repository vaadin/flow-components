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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/grid-order-columns")
public class GridOrderColumnsPage extends VerticalLayout {
    public GridOrderColumnsPage() {
        Grid<Integer> grid = new Grid<>();
        grid.appendHeaderRow();
        grid.appendFooterRow();

        Grid.Column<Integer> column1 = grid.addColumn(value -> "col1 " + value)
                .setHeader("Col1").setKey("1");
        Grid.Column<Integer> column2 = grid.addColumn(value -> "col2 " + value)
                .setHeader("Col2").setKey("2");
        Grid.Column<Integer> column3 = grid.addColumn(value -> "col3 " + value)
                .setHeader("Col3").setKey("3");
        Grid.Column<Integer> column4 = grid.addColumn(value -> "col4 " + value)
                .setHeader("Col4").setKey("4");

        // Adding invisible column to make sure it's not affecting ordering
        // of the visible columns
        column4.setVisible(false);

        ListDataProvider<Integer> dataProvider = DataProvider.ofItems(1, 2, 3,
                4, 5);
        grid.setDataProvider(dataProvider);
        add(grid);

        Button orderCol123Button = new Button("Col 1 2 3 ",
                e -> grid.setColumnOrder(column1, column2, column3, column4));
        orderCol123Button.setId("button-123");
        Button orderCol321Button = new Button("Col 3 2 1 ",
                e -> grid.setColumnOrder(column4, column3, column2, column1));
        orderCol321Button.setId("button-321");

        Button orderCol31Button = new Button("order only the columns 3 and 1 ",
                e -> {
                    grid.removeColumn(column2);
                    grid.setColumnOrder(column4, column3, column1);
                });
        orderCol31Button.setId("button-31");

        Button visualOrderButton = new Button("Visual 2 1 3",
                e -> grid.getElement().executeJs(
                        "this._swapColumnOrders($0, $1)", column1.getElement(),
                        column2.getElement()));
        visualOrderButton.setId("button-visual-order");

        add(new HorizontalLayout(orderCol123Button, orderCol321Button,
                orderCol31Button, visualOrderButton));
    }
}
