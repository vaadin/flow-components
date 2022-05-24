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

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.bean.Item;
import com.vaadin.flow.data.bean.ItemGenerator;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/using-renderers")
public class GridViewUsingRenderersPage extends LegacyTestView {

    public GridViewUsingRenderersPage() {
        createBasicRenderers();
    }

    private void createBasicRenderers() {
        Grid<Item> grid = new Grid<>();
        grid.setItems(getShoppingCart());

        grid.addColumn(Item::getName).setHeader("Name");

        // NumberRenderer to render numbers in general
        grid.addColumn(new NumberRenderer<>(Item::getPrice, "$ %(,.2f",
                Locale.US, "$ 0.00")).setHeader("Price");

        // LocalDateTimeRenderer for date and time
        grid.addColumn(new LocalDateTimeRenderer<>(Item::getPurchaseDate,
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT,
                        FormatStyle.MEDIUM)))
                .setHeader("Purchase date and time").setFlexGrow(2);

        // LocalDateRenderer for dates
        grid.addColumn(new LocalDateRenderer<>(Item::getEstimatedDeliveryDate,
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
                .setHeader("Estimated delivery date");

        // Icons
        grid.addColumn(new IconRenderer<>(
                item -> item.getPrice() > 50 ? new Span("$$$") : new Span("$"),
                item -> ""));

        // NativeButtonRenderer for an easy clickable button,
        // without creating a component
        grid.addColumn(new NativeButtonRenderer<>("Remove", item -> {
            ListDataProvider<Item> dataProvider = (ListDataProvider<Item>) grid
                    .getDataProvider();
            dataProvider.getItems().remove(item);
            dataProvider.refreshAll();
        })).setWidth("100px").setFlexGrow(0);

        grid.setId("grid-basic-renderers");
        addCard("Using renderers", "Using basic renderers", grid);
    }

    private static List<Item> getShoppingCart() {
        return new ItemGenerator().generateItems(100);
    }
}
