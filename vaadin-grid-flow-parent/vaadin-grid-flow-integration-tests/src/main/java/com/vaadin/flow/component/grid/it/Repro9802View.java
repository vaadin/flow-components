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

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/9802
 *
 * Query parameters (all optional):
 * <ul>
 * <li><code>mode</code>: <code>hack</code> (default, reporter's shadow DOM
 * flex-grow tweak), <code>allrows</code> (allRowsVisible instead of the tweak),
 * <code>plain</code> (no tweak at all)</li>
 * <li><code>total</code>: total item count, default 43</li>
 * <li><code>filtered</code>: item count while filtered, default 20</li>
 * <li><code>gridHeight</code>: explicit grid height, e.g. <code>800px</code>.
 * Default: grid grows to fill the full-height parent layout.</li>
 * </ul>
 */
@Route("repro-9802")
public class Repro9802View extends VerticalLayout
        implements HasUrlParameter<String> {

    private final Span itemsHeight = new Span();

    @Override
    public void setParameter(BeforeEvent event,
            @OptionalParameter String parameter) {
        var params = event.getLocation().getQueryParameters().getParameters();
        String mode = params.getOrDefault("mode", List.of("hack")).get(0);
        int total = Integer
                .parseInt(params.getOrDefault("total", List.of("43")).get(0));
        int filtered = Integer.parseInt(
                params.getOrDefault("filtered", List.of("20")).get(0));
        String gridHeight = params.getOrDefault("gridHeight", List.of(""))
                .get(0);

        removeAll();
        build(mode, total, filtered, gridHeight);
    }

    private void build(String mode, int total, int filtered,
            String gridHeight) {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            items.add(new Item("Street " + i, String.valueOf(i),
                    "1234" + i % 10, "City " + i,
                    i < filtered ? "Italien" : "Deutschland"));
        }

        Grid<Item> grid = new Grid<>();
        grid.setId("grid");
        var streetColumn = grid.addColumn(Item::street).setHeader("Street");
        grid.addColumn(Item::houseNumber).setHeader("No.");
        grid.addColumn(Item::postalCode).setHeader("Postal Code");
        grid.addColumn(Item::city).setHeader("City");
        grid.addColumn(Item::country).setHeader("Country");

        if ("hack".equals(mode)) {
            // Reporter's tweak: let the items container shrink to its content
            // and the footer take the remaining space.
            grid.getElement().executeJs(
                    "this.shadowRoot.querySelector('#items').style.flexGrow = '0';"
                            + "this.shadowRoot.querySelector('#items').style.flexShrink = '1';"
                            + "this.shadowRoot.querySelector('#footer').style.flexGrow = '1';");
        } else if ("allrows".equals(mode)) {
            grid.setAllRowsVisible(true);
        }

        GridListDataView<Item> dataView = grid.setItems(items);

        FooterRow.FooterCell footerCell = grid.appendFooterRow()
                .getCell(streetColumn);
        Runnable updateFooter = () -> footerCell
                .setText(dataView.getItemCount() + " entries");
        updateFooter.run();

        NativeButton filter = new NativeButton("Show only Italy", e -> {
            dataView.setFilter(item -> "Italien".equals(item.country()));
            updateFooter.run();
        });
        filter.setId("filter");
        NativeButton clear = new NativeButton("Clear filter", e -> {
            dataView.removeFilters();
            updateFooter.run();
        });
        clear.setId("clear");
        NativeButton logHeights = new NativeButton("Log heights",
                e -> logHeights(grid));
        logHeights.setId("log-heights");
        itemsHeight.setId("items-height");

        var toolbar = new HorizontalLayout(new Span("mode=" + mode), filter,
                clear, logHeights, itemsHeight);
        toolbar.setWrap(true);
        toolbar.setWidthFull();

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        add(toolbar, grid);
        if (gridHeight.isEmpty()) {
            setFlexGrow(1, grid);
        } else {
            grid.setHeight(gridHeight);
        }
    }

    private void logHeights(Grid<Item> grid) {
        grid.getElement().executeJs(
                "const items = this.shadowRoot.querySelector('#items');"
                        + "const footer = this.shadowRoot.querySelector('#footer');"
                        + "return 'grid=' + this.getBoundingClientRect().height"
                        + " + ' items=' + items.getBoundingClientRect().height"
                        + " + ' items.style.height=' + items.style.height"
                        + " + ' footerTop=' + footer.getBoundingClientRect().top"
                        + " + ' rows=' + this.shadowRoot.querySelectorAll('#items tr:not([hidden])').length;")
                .then(String.class, itemsHeight::setText);
    }

    private record Item(String street, String houseNumber, String postalCode,
            String city, String country) {
    }
}
