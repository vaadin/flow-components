/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-range-selection")
public class GridRangeSelectionPage extends Div {
    private String startItem0 = null;
    private String startItem1 = null;

    public GridRangeSelectionPage() {
        addGrid0();
        addGrid1();
    }

    private void addGrid0() {
        Grid<String> grid = new Grid<>();
        grid.addColumn(s -> s);
        grid.setItems(createItems(0, 1000));
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.addClientItemSelectionListener(event -> {
            String item = event.getItem();

            startItem0 = startItem0 != null ? startItem0 : item;
            if (event.isShiftKey()) {
                Set<String> range = getItemsRange(grid, startItem0, item);
                if (event.isSelected()) {
                    grid.asMultiSelect().select(range);
                } else {
                    grid.asMultiSelect().deselect(range);
                }
            }
            startItem0 = item;
        });
        add(grid);
    }

    private void addGrid1() {
        Grid<String> grid = new Grid<>();
        grid.addColumn(s -> s);
        grid.setItems(createItems(0, 1000));
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.addClientItemSelectionListener(event -> {
            String item = event.getItem();

            startItem1 = startItem1 != null ? startItem1 : item;
            if (event.isSelected()) {
                if (event.isShiftKey()) {
                    Set<String> range = getItemsRange(grid, startItem1, item);
                    grid.asMultiSelect().updateSelection(range,
                            grid.getSelectedItems());
                    return;
                }

                grid.asMultiSelect().updateSelection(Set.of(item),
                        grid.getSelectedItems());
                startItem1 = item;
            } else {
                if (event.isShiftKey() && !item.equals(startItem1)) {
                    Set<String> range = getItemsRange(grid, startItem1, item);
                    grid.asMultiSelect().updateSelection(range,
                            grid.getSelectedItems());
                    return;
                }

                grid.deselectAll();
                startItem1 = null;
            }
        });
        add(grid);
    }

    private Set<String> getItemsRange(Grid<String> grid, String startItem,
            String endItem) {
        var dataView = grid.getListDataView();
        var startIndex = dataView.getItemIndex(startItem).get();
        var endIndex = dataView.getItemIndex(endItem).get();

        return dataView.getItems().skip(Math.min(startIndex, endIndex))
                .limit(Math.abs(startIndex - endIndex) + 1)
                .collect(Collectors.toSet());
    }

    private List<String> createItems(int start, int end) {
        return IntStream.range(start, end).mapToObj(Integer::toString).toList();
    }
}
