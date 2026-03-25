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
package com.vaadin.flow.component.grid;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.tests.MockUIExtension;

class AbstractGridMultiSelectionModelSelectedItemsTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Grid<String> grid;
    private String item1;
    private String item2;
    private String item3;
    private AbstractGridMultiSelectionModel<String> selectionModel;

    @BeforeEach
    void init() {
        item1 = "item1";
        item2 = "item2";
        item3 = "item3";

        ListDataProvider<String> dataProvider = new ListDataProvider<>(
                List.of(item1, item2, item3)) {
            @Override
            public Object getId(String item) {
                return System.identityHashCode(item);
            }
        };

        grid = new Grid<>() {
            @Override
            boolean isInActiveRange(String item) {
                // Updates are sent only for items loaded by client
                return true;
            }
        };
        grid.setItems(dataProvider);
        grid.setSelectionMode(SelectionMode.MULTI);

        selectionModel = ((AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel());

        ui.add(grid);
    }

    @Test
    void singleSelect() {
        grid.select(item1);
        verifySelection(item1);
    }

    @Test
    void singleSelectItemMultipleTimes() {
        grid.select(item1);
        grid.select(item1);
        verifySelection(item1);
    }

    @Test
    void singleSelectItemMultipleTimesAndDeselect() {
        grid.select(item1);
        grid.select(item1);
        grid.deselect(item1);
        verifySelection();
    }

    @Test
    void singleSelectItemAndDeselectMultipleTimes() {
        grid.select(item1);
        grid.select(item2);
        grid.deselect(item1);
        grid.deselect(item1);
        verifySelection(item2);
    }

    @Test
    void singleSelectMultipleItems() {
        grid.select(item1);
        grid.select(item2);
        grid.select(item3);
        verifySelection(item1, item2, item3);
    }

    @Test
    void singleSelectMultipleItemsAndDeselect() {
        grid.select(item1);
        grid.select(item2);
        grid.select(item3);
        grid.deselect(item2);
        verifySelection(item1, item3);
    }

    @Test
    void singleSelectMultipleItemsAndDeselectAll() {
        grid.select(item1);
        grid.select(item2);
        grid.select(item3);
        grid.deselectAll();
        verifySelection();
    }

    @Test
    void multiSelect() {
        grid.asMultiSelect().select(item1, item2, item3);
        verifySelection(item1, item2, item3);
    }

    @Test
    void multiSelectItemsMultipleTimes() {
        grid.asMultiSelect().select(item1, item2, item3);
        grid.asMultiSelect().select(item1, item2, item3);
        verifySelection(item1, item2, item3);
    }

    @Test
    void multiSelectItemsMultipleTimesAndDeselect() {
        grid.asMultiSelect().select(item1, item2, item3);
        grid.asMultiSelect().select(item1, item2, item3);
        grid.asMultiSelect().deselect(item1, item2);
        verifySelection(item3);
    }

    @Test
    void multiSelectItemsAndDeselectMultipleTimes() {
        grid.asMultiSelect().select(item1, item2, item3);
        grid.asMultiSelect().deselect(item1, item2);
        grid.asMultiSelect().deselect(item1, item2);
        verifySelection(item3);
    }

    @Test
    void multiSelectAndDeselectAll() {
        grid.asMultiSelect().select(item1, item2, item3);
        grid.deselectAll();
        verifySelection();
    }

    @Test
    void multiSelectAndDeselect() {
        MultiSelect<Grid<String>, String> multiSelect = grid.asMultiSelect();
        multiSelect.select(item1, item2, item3);
        multiSelect.deselect(item1, item3);
        verifySelection(item2);
    }

    private void verifySelection(String... values) {
        Assertions.assertEquals(Set.of(values),
                selectionModel.getSelectedItems());
        Assertions.assertEquals(
                Stream.of(values).map(grid.getDataProvider()::getId)
                        .collect(Collectors.toSet()),
                selectionModel.getSelectedItemIds());
        for (String value : values) {
            Assertions.assertTrue(selectionModel.isSelected(value));
        }
    }
}
