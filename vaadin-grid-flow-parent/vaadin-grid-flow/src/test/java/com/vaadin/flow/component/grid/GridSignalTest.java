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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class GridSignalTest extends AbstractSignalsUnitTest {

    private Grid<String> grid;
    private Grid.Column<String> column;

    @Before
    public void setup() {
        grid = new Grid<>();
        column = grid.addColumn(s -> s);
    }

    @After
    public void tearDown() {
        if (grid != null && grid.isAttached()) {
            grid.removeFromParent();
        }
    }

    // ===== COLUMN BIND HEADER TESTS =====

    @Test
    public void bindHeader_signalBound_headerSynchronizedWhenAttached() {
        var headerSignal = new ValueSignal<>("Header");
        column.bindHeader(headerSignal);
        UI.getCurrent().add(grid);

        // Verify header text is set by checking the default header row
        var headerRow = grid.getDefaultHeaderRow();
        Assert.assertNotNull(headerRow);
        Assert.assertEquals("Header", headerRow.getCell(column).getText());

        headerSignal.set("Updated Header");
        Assert.assertEquals("Updated Header",
                headerRow.getCell(column).getText());
    }

    @Test
    public void bindHeader_signalBound_noEffectWhenDetached() {
        var headerSignal = new ValueSignal<>("Header");
        column.bindHeader(headerSignal);
        UI.getCurrent().add(grid);
        Assert.assertEquals("Header",
                grid.getDefaultHeaderRow().getCell(column).getText());

        // Detach
        grid.removeFromParent();
        headerSignal.set("Updated");
        Assert.assertEquals("Header",
                grid.getDefaultHeaderRow().getCell(column).getText());
    }

    @Test(expected = BindingActiveException.class)
    public void bindHeader_setHeaderWhileBound_throwsException() {
        var headerSignal = new ValueSignal<>("Header");
        column.bindHeader(headerSignal);
        UI.getCurrent().add(grid);

        column.setHeader("manual");
    }

    @Test(expected = BindingActiveException.class)
    public void bindHeader_bindAgainWhileBound_throwsException() {
        var headerSignal = new ValueSignal<>("Header");
        column.bindHeader(headerSignal);
        UI.getCurrent().add(grid);

        column.bindHeader(new ValueSignal<>("Other"));
    }

    // ===== COLUMN BIND FOOTER TESTS =====

    @Test
    public void bindFooter_signalBound_footerSynchronizedWhenAttached() {
        var footerSignal = new ValueSignal<>("Footer");
        column.bindFooter(footerSignal);
        UI.getCurrent().add(grid);

        // Verify footer text is set
        var footerRows = grid.getFooterRows();
        Assert.assertFalse(footerRows.isEmpty());
        Assert.assertEquals("Footer",
                footerRows.get(0).getCell(column).getText());

        footerSignal.set("Updated Footer");
        Assert.assertEquals("Updated Footer",
                footerRows.get(0).getCell(column).getText());
    }

    @Test
    public void bindFooter_signalBound_noEffectWhenDetached() {
        var footerSignal = new ValueSignal<>("Footer");
        column.bindFooter(footerSignal);
        UI.getCurrent().add(grid);
        Assert.assertEquals("Footer",
                grid.getFooterRows().get(0).getCell(column).getText());

        // Detach
        grid.removeFromParent();
        footerSignal.set("Updated");
        Assert.assertEquals("Footer",
                grid.getFooterRows().get(0).getCell(column).getText());
    }

    @Test(expected = BindingActiveException.class)
    public void bindFooter_setFooterWhileBound_throwsException() {
        var footerSignal = new ValueSignal<>("Footer");
        column.bindFooter(footerSignal);
        UI.getCurrent().add(grid);

        column.setFooter("manual");
    }

    @Test(expected = BindingActiveException.class)
    public void bindFooter_bindAgainWhileBound_throwsException() {
        var footerSignal = new ValueSignal<>("Footer");
        column.bindFooter(footerSignal);
        UI.getCurrent().add(grid);

        column.bindFooter(new ValueSignal<>("Other"));
    }

    // ===== GRID BIND EMPTY STATE TEXT TESTS =====

    @Test
    public void bindEmptyStateText_signalBound_textSynchronizedWhenAttached() {
        var emptyStateSignal = new ValueSignal<>("No data");
        grid.bindEmptyStateText(emptyStateSignal);
        UI.getCurrent().add(grid);

        Assert.assertEquals("No data", grid.getEmptyStateText());

        emptyStateSignal.set("No items found");
        Assert.assertEquals("No items found", grid.getEmptyStateText());
    }

    @Test
    public void bindEmptyStateText_signalBound_noEffectWhenDetached() {
        var emptyStateSignal = new ValueSignal<>("No data");
        grid.bindEmptyStateText(emptyStateSignal);
        // Not attached to UI

        String initial = grid.getEmptyStateText();
        emptyStateSignal.set("Updated");
        Assert.assertEquals(initial, grid.getEmptyStateText());
    }

    @Test
    public void bindEmptyStateText_signalBound_detachAndReattach() {
        var emptyStateSignal = new ValueSignal<>("No data");
        grid.bindEmptyStateText(emptyStateSignal);
        UI.getCurrent().add(grid);
        Assert.assertEquals("No data", grid.getEmptyStateText());

        // Detach
        grid.removeFromParent();
        emptyStateSignal.set("Updated");
        Assert.assertEquals("No data", grid.getEmptyStateText());

        // Reattach
        UI.getCurrent().add(grid);
        Assert.assertEquals("Updated", grid.getEmptyStateText());
    }

    @Test(expected = BindingActiveException.class)
    public void bindEmptyStateText_setEmptyStateTextWhileBound_throwsException() {
        var emptyStateSignal = new ValueSignal<>("No data");
        grid.bindEmptyStateText(emptyStateSignal);
        UI.getCurrent().add(grid);

        grid.setEmptyStateText("manual");
    }

    @Test(expected = BindingActiveException.class)
    public void bindEmptyStateText_bindAgainWhileBound_throwsException() {
        var emptyStateSignal = new ValueSignal<>("No data");
        grid.bindEmptyStateText(emptyStateSignal);
        UI.getCurrent().add(grid);

        grid.bindEmptyStateText(new ValueSignal<>("Other"));
    }

    // ===== GRID BIND COLUMNS TESTS =====

    @Test
    public void bindColumns_signalBound_columnsSynchronizedWhenAttached() {
        var beanGrid = new Grid<>(Person.class);
        var columnsSignal = new ValueSignal<>(List.of("name"));
        beanGrid.bindColumns(columnsSignal);
        UI.getCurrent().add(beanGrid);

        Assert.assertEquals(1, beanGrid.getColumns().size());

        columnsSignal.set(List.of("name", "born"));
        Assert.assertEquals(2, beanGrid.getColumns().size());
    }

    @Test
    public void bindColumns_signalBound_noEffectWhenDetached() {
        var beanGrid = new Grid<>(Person.class);
        var columnsSignal = new ValueSignal<>(List.of("name"));
        beanGrid.bindColumns(columnsSignal);
        UI.getCurrent().add(beanGrid);
        Assert.assertEquals(1, beanGrid.getColumns().size());

        beanGrid.removeFromParent();
        columnsSignal.set(List.of("name", "born"));
        Assert.assertEquals(1, beanGrid.getColumns().size());
    }

    @Test(expected = BindingActiveException.class)
    public void bindColumns_setColumnsWhileBound_throwsException() {
        var beanGrid = new Grid<>(Person.class);
        var columnsSignal = new ValueSignal<>(List.of("name"));
        beanGrid.bindColumns(columnsSignal);
        UI.getCurrent().add(beanGrid);

        beanGrid.setColumns("name", "born");
    }

    @Test(expected = BindingActiveException.class)
    public void bindColumns_bindAgainWhileBound_throwsException() {
        var beanGrid = new Grid<>(Person.class);
        var columnsSignal = new ValueSignal<>(List.of("name"));
        beanGrid.bindColumns(columnsSignal);
        UI.getCurrent().add(beanGrid);

        beanGrid.bindColumns(new ValueSignal<>(List.of("born")));
    }

    // ===== SINGLE SELECT BIND VALUE TESTS =====

    @Test
    public void singleSelect_bindValue_signalSetsSelection() {
        grid.setItems("foo", "bar", "baz");
        var signal = new ValueSignal<>("foo");
        grid.asSingleSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assert.assertEquals("foo", grid.asSingleSelect().getValue());

        signal.set("bar");
        Assert.assertEquals("bar", grid.asSingleSelect().getValue());
    }

    @Test
    public void singleSelect_bindValue_clientSelectionUpdatesSignal() {
        grid.setItems("foo", "bar", "baz");
        var signal = new ValueSignal<String>(null);
        grid.asSingleSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assert.assertNull(signal.peek());

        ((GridSingleSelectionModel<String>) grid.getSelectionModel())
                .selectFromClient("bar");

        Assert.assertEquals("bar", signal.peek());
    }

    @Test(expected = BindingActiveException.class)
    public void singleSelect_bindValue_bindAgainWhileBound_throwsException() {
        grid.setItems("foo", "bar");
        var signal = new ValueSignal<String>(null);
        grid.asSingleSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        grid.asSingleSelect().bindValue(new ValueSignal<>("foo"), v -> {
        });
    }

    @Test
    public void singleSelect_bindValue_noEffectWhenDetached() {
        grid.setItems("foo", "bar", "baz");
        var signal = new ValueSignal<>("foo");
        grid.asSingleSelect().bindValue(signal, signal::set);
        // Not attached

        signal.set("bar");
        Assert.assertNull(grid.asSingleSelect().getValue());
    }

    // ===== MULTI SELECT BIND VALUE TESTS =====

    @Test
    public void multiSelect_bindValue_signalSetsSelection() {
        grid.setItems("foo", "bar", "baz");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var signal = new ValueSignal<>(Set.of("foo", "bar"));
        grid.asMultiSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assert.assertEquals(Set.of("foo", "bar"),
                grid.asMultiSelect().getValue());

        signal.set(Set.of("baz"));
        Assert.assertEquals(Set.of("baz"), grid.asMultiSelect().getValue());
    }

    @Test
    public void multiSelect_bindValue_clientSelectionUpdatesSignal() {
        grid.setItems("foo", "bar", "baz");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var signal = new ValueSignal<Set<String>>(Set.of());
        grid.asMultiSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assert.assertEquals(Set.of(), signal.peek());

        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectFromClient("bar");

        Assert.assertEquals(Set.of("bar"), signal.peek());
    }

    @Test(expected = BindingActiveException.class)
    public void multiSelect_bindValue_bindAgainWhileBound_throwsException() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var signal = new ValueSignal<Set<String>>(Set.of());
        grid.asMultiSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        grid.asMultiSelect().bindValue(new ValueSignal<>(Set.of("foo")), v -> {
        });
    }
}
