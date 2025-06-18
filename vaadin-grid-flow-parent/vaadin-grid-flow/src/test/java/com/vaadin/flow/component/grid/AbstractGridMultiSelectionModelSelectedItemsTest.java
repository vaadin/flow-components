/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.MultiSelect;

public class AbstractGridMultiSelectionModelSelectedItemsTest {

    private DataCommunicatorTest.MockUI ui;
    private Grid<String> grid;
    private String item1;
    private String item2;
    private String item3;
    private AbstractGridMultiSelectionModel<String> selectionModel;

    @Before
    public void init() {
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

        ui = new DataCommunicatorTest.MockUI();
        ui.add(grid);
    }

    @Test
    public void singleSelect() {
        grid.select(item1);
        verifySelection(item1);
    }

    @Test
    public void singleSelectItemMultipleTimes() {
        grid.select(item1);
        grid.select(item1);
        verifySelection(item1);
    }

    @Test
    public void singleSelectItemMultipleTimesAndDeselect() {
        grid.select(item1);
        grid.select(item1);
        grid.deselect(item1);
        verifySelection();
    }

    @Test
    public void singleSelectItemAndDeselectMultipleTimes() {
        grid.select(item1);
        grid.select(item2);
        grid.deselect(item1);
        grid.deselect(item1);
        verifySelection(item2);
    }

    @Test
    public void singleSelectMultipleItems() {
        grid.select(item1);
        grid.select(item2);
        grid.select(item3);
        verifySelection(item1, item2, item3);
    }

    @Test
    public void singleSelectMultipleItemsAndDeselect() {
        grid.select(item1);
        grid.select(item2);
        grid.select(item3);
        grid.deselect(item2);
        verifySelection(item1, item3);
    }

    @Test
    public void singleSelectMultipleItemsAndDeselectAll() {
        grid.select(item1);
        grid.select(item2);
        grid.select(item3);
        grid.deselectAll();
        verifySelection();
    }

    @Test
    public void multiSelect() {
        grid.asMultiSelect().select(item1, item2, item3);
        verifySelection(item1, item2, item3);
    }

    @Test
    public void multiSelectItemsMultipleTimes() {
        grid.asMultiSelect().select(item1, item2, item3);
        grid.asMultiSelect().select(item1, item2, item3);
        verifySelection(item1, item2, item3);
    }

    @Test
    public void multiSelectItemsMultipleTimesAndDeselect() {
        grid.asMultiSelect().select(item1, item2, item3);
        grid.asMultiSelect().select(item1, item2, item3);
        grid.asMultiSelect().deselect(item1, item2);
        verifySelection(item3);
    }

    @Test
    public void multiSelectItemsAndDeselectMultipleTimes() {
        grid.asMultiSelect().select(item1, item2, item3);
        grid.asMultiSelect().deselect(item1, item2);
        grid.asMultiSelect().deselect(item1, item2);
        verifySelection(item3);
    }

    @Test
    public void multiSelectAndDeselectAll() {
        grid.asMultiSelect().select(item1, item2, item3);
        grid.deselectAll();
        verifySelection();
    }

    @Test
    public void multiSelectAndDeselect() {
        MultiSelect<Grid<String>, String> multiSelect = grid.asMultiSelect();
        multiSelect.select(item1, item2, item3);
        multiSelect.deselect(item1, item3);
        verifySelection(item2);
    }

    private void verifySelection(String... values) {
        Assert.assertEquals(Set.of(values), selectionModel.getSelectedItems());
        Assert.assertEquals(
                Stream.of(values).map(grid.getDataProvider()::getId)
                        .collect(Collectors.toSet()),
                selectionModel.getSelectedItemIds());
        for (String value : values) {
            Assert.assertTrue(selectionModel.isSelected(value));
        }
    }
}
