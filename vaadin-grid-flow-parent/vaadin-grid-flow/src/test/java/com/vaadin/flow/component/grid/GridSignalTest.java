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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class GridSignalTest extends AbstractSignalsUnitTest {

    private Grid<String> grid;

    @Before
    public void setup() {
        grid = new Grid<>();
    }

    @After
    public void tearDown() {
        if (grid != null && grid.isAttached()) {
            grid.removeFromParent();
        }
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
        // Not attached — initial probe sets "foo", then effect is passivated

        // Subsequent signal changes while detached should have no effect
        signal.set("bar");
        Assert.assertEquals("foo", grid.asSingleSelect().getValue());
    }

    @Test
    public void singleSelect_bindValue_signalChangeDoesNotInvokeWriteCallback() {
        grid.setItems("foo", "bar", "baz");
        int[] callCount = { 0 };
        var signal = new ValueSignal<String>(null);
        grid.asSingleSelect().bindValue(signal, v -> callCount[0]++);
        UI.getCurrent().add(grid);

        // Signal-originated change should not trigger the write callback
        signal.set("bar");
        Assert.assertEquals("bar", grid.asSingleSelect().getValue());
        Assert.assertEquals(0, callCount[0]);

        // Client-originated change should trigger the write callback
        ((GridSingleSelectionModel<String>) grid.getSelectionModel())
                .selectFromClient("baz");
        Assert.assertEquals(1, callCount[0]);
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

    @Test
    public void multiSelect_bindValue_signalChangeDoesNotInvokeWriteCallback() {
        grid.setItems("foo", "bar", "baz");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        int[] callCount = { 0 };
        var signal = new ValueSignal<Set<String>>(Set.of());
        grid.asMultiSelect().bindValue(signal, v -> callCount[0]++);
        UI.getCurrent().add(grid);

        // Signal-originated change should not trigger the write callback
        signal.set(Set.of("foo", "bar"));
        Assert.assertEquals(Set.of("foo", "bar"),
                grid.asMultiSelect().getValue());
        Assert.assertEquals(0, callCount[0]);

        // Client-originated change should trigger the write callback
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectFromClient("baz");
        Assert.assertEquals(1, callCount[0]);
    }

    // ===== SELECTION MODEL SWITCH CLEANUP TESTS =====

    @Test
    public void singleSelect_bindValue_switchToMulti_bindingCleanedUp() {
        grid.setItems("foo", "bar", "baz");
        var signal = new ValueSignal<>("foo");
        grid.asSingleSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assert.assertEquals("foo", grid.asSingleSelect().getValue());

        // Switch to multi-select; the old binding should be cleaned up
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        // Switch back to single-select so SingleSelectionEvent is active again
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        // Track whether a spurious selection event fires from the old effect
        boolean[] eventFired = { false };
        grid.asSingleSelect().addValueChangeListener(e -> eventFired[0] = true);

        // Changing the old signal should not trigger the old effect
        signal.set("bar");
        Assert.assertFalse(
                "Old signal effect should not fire after model switch",
                eventFired[0]);
        Assert.assertNull(grid.asSingleSelect().getValue());
    }

    @Test
    public void singleSelect_bindValue_switchToMulti_canBindNewSignal() {
        grid.setItems("foo", "bar", "baz");
        var signal = new ValueSignal<>("foo");
        grid.asSingleSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        // Switch to multi-select
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        // Should be able to bind a new signal without BindingActiveException
        var multiSignal = new ValueSignal<>(Set.of("bar"));
        grid.asMultiSelect().bindValue(multiSignal, multiSignal::set);

        Assert.assertEquals(Set.of("bar"), grid.asMultiSelect().getValue());
    }

    @Test
    public void multiSelect_bindValue_switchToSingle_bindingCleanedUp() {
        grid.setItems("foo", "bar", "baz");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var signal = new ValueSignal<>(Set.of("foo", "bar"));
        grid.asMultiSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assert.assertEquals(Set.of("foo", "bar"),
                grid.asMultiSelect().getValue());

        // Switch to single-select; the old binding should be cleaned up
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        // Switch back to multi-select so MultiSelectionEvent is active again
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        // Track whether a spurious selection event fires from the old effect
        boolean[] eventFired = { false };
        grid.asMultiSelect().addValueChangeListener(e -> eventFired[0] = true);

        // Changing the old signal should not trigger the old effect
        signal.set(Set.of("baz"));
        Assert.assertFalse(
                "Old signal effect should not fire after model switch",
                eventFired[0]);
        Assert.assertTrue(grid.asMultiSelect().getValue().isEmpty());
    }

    @Test
    public void multiSelect_bindValue_switchToSingle_canBindNewSignal() {
        grid.setItems("foo", "bar", "baz");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var signal = new ValueSignal<>(Set.of("foo"));
        grid.asMultiSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        // Switch to single-select
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        // Should be able to bind a new signal without BindingActiveException
        var singleSignal = new ValueSignal<>("bar");
        grid.asSingleSelect().bindValue(singleSignal, singleSignal::set);

        Assert.assertEquals("bar", grid.asSingleSelect().getValue());
    }

    // ===== BIND ITEMS TESTS =====

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetDataProvider_throws() {
        var grid = createGridWithBoundItems();
        grid.setDataProvider(DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithDataProvider_throws() {
        var grid = createGridWithBoundItems();
        grid.setItems(DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithInMemoryDataProvider_throws() {
        var grid = createGridWithBoundItems();
        grid.setItems(DataProvider
                .ofCollection(Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithListDataProvider_throws() {
        var grid = createGridWithBoundItems();
        grid.setItems(new ListDataProvider<>(
                Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithCollection_throws() {
        var grid = createGridWithBoundItems();
        grid.setItems(Arrays.asList("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithVarargs_throws() {
        var grid = createGridWithBoundItems();
        grid.setItems("New Item 1", "New Item 2");
    }

    @Test
    public void signalConstructor_setsItemsFromSignal() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("One");
        listSignal.insertLast("Two");

        Grid<String> grid = new Grid<>(listSignal);
        ui.add(grid);

        List<String> items = grid.getGenericDataView().getItems().toList();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("One", items.get(0));
        Assert.assertEquals("Two", items.get(1));

        listSignal.insertLast("Three");

        items = grid.getGenericDataView().getItems().toList();
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("Three", items.get(2));
    }

    @Test
    public void singleSelect_bindItems_selectItem_updateIdentity_selectionPreserved() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("a");
        listSignal.insertLast("b");

        grid.bindItems(listSignal);
        ui.add(grid);

        grid.asSingleSelect().setValue("a");
        Assert.assertEquals("a", grid.asSingleSelect().getValue());

        // Change the identity of the selected item
        listSignal.peek().getFirst().set("a-updated");

        // Verify selection is preserved with the new item
        Assert.assertEquals("a-updated", grid.asSingleSelect().getValue());
    }

    @Test
    public void multiSelect_bindItems_selectItem_updateIdentity_selectionPreserved() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("a");
        listSignal.insertLast("b");

        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.bindItems(listSignal);
        ui.add(grid);

        grid.asMultiSelect().setValue(Set.of("a"));
        Assert.assertEquals(Set.of("a"), grid.asMultiSelect().getValue());

        // Change the identity of the selected item
        listSignal.peek().getFirst().set("a-updated");

        // Verify selection is preserved with the new item
        Assert.assertEquals(Set.of("a-updated"),
                grid.asMultiSelect().getValue());
    }

    private Grid<String> createGridWithBoundItems() {
        var grid = new Grid<String>();
        var itemsSignal = new ListSignal<String>();
        itemsSignal.insertLast("Item 1");
        itemsSignal.insertLast("Item 2");
        grid.bindItems(itemsSignal);
        ui.add(grid);
        return grid;
    }
}
