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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class GridMultiSelectSignalTest extends AbstractSignalsTest {

    private Grid<String> grid;
    private final List<Set<String>> multiSelectEvents = new ArrayList<>();

    @BeforeEach
    void setup() {
        grid = new Grid<>();
    }

    @AfterEach
    void tearDown() {
        if (grid != null && grid.isAttached()) {
            grid.removeFromParent();
        }
    }

    @Test
    void bindValue_signalSetsSelection() {
        grid.setItems("foo", "bar", "baz");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var signal = new ValueSignal<>(Set.of("foo", "bar"));
        grid.asMultiSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assertions.assertEquals(Set.of("foo", "bar"),
                grid.asMultiSelect().getValue());

        signal.set(Set.of("baz"));
        Assertions.assertEquals(Set.of("baz"), grid.asMultiSelect().getValue());
    }

    @Test
    void bindValue_clientSelectionUpdatesSignal() {
        grid.setItems("foo", "bar", "baz");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var signal = new ValueSignal<Set<String>>(Set.of());
        grid.asMultiSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assertions.assertEquals(Set.of(), signal.peek());

        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectFromClient("bar");

        Assertions.assertEquals(Set.of("bar"), signal.peek());
    }

    @Test
    void bindValue_bindAgainWhileBound_throwsException() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var signal = new ValueSignal<Set<String>>(Set.of());
        grid.asMultiSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assertions.assertThrows(BindingActiveException.class,
                () -> grid.asMultiSelect()
                        .bindValue(new ValueSignal<>(Set.of("foo")), v -> {
                        }));
    }

    @Test
    void bindValue_nullWriteCallback_throwsException() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var signal = new ValueSignal<Set<String>>(Set.of());

        Assertions.assertThrows(NullPointerException.class,
                () -> grid.asMultiSelect().bindValue(signal, null));
    }

    @Test
    void bindValue_signalChangeDoesNotInvokeWriteCallback() {
        grid.setItems("foo", "bar", "baz");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        int[] callCount = { 0 };
        var signal = new ValueSignal<Set<String>>(Set.of());
        grid.asMultiSelect().bindValue(signal, v -> callCount[0]++);
        UI.getCurrent().add(grid);

        // Signal-originated change should not trigger the write callback
        signal.set(Set.of("foo", "bar"));
        Assertions.assertEquals(Set.of("foo", "bar"),
                grid.asMultiSelect().getValue());
        Assertions.assertEquals(0, callCount[0]);

        // Client-originated change should trigger the write callback
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectFromClient("baz");
        Assertions.assertEquals(1, callCount[0]);
    }

    @Test
    void bindValue_writeCallbackRefuses_selectionRevertedWithoutEvent() {
        grid.setItems("foo", "bar", "baz");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var signal = new ValueSignal<>(Set.of("foo"));
        grid.asMultiSelect().bindValue(signal, v -> {
        });
        UI.getCurrent().add(grid);
        List<Set<String>> events = new ArrayList<>();
        grid.asMultiSelect()
                .addValueChangeListener(e -> events.add(e.getValue()));

        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectFromClient("bar");
        Assertions.assertEquals(Set.of("foo"), grid.asMultiSelect().getValue());

        grid.asMultiSelect().select("baz");
        Assertions.assertEquals(Set.of("foo"), grid.asMultiSelect().getValue());

        Assertions.assertEquals(Set.of("foo"), signal.peek());
        Assertions.assertEquals(List.of(), events);
    }

    @Test
    void bindValue_writeCallbackRefusesSelectAll_selectionAndCheckboxReverted() {
        var model = bindRefusingMultiSelect(Set.of("foo"));

        model.selectAll();

        Assertions.assertEquals(Set.of("foo"), grid.asMultiSelect().getValue());
        assertSelectAllCheckboxState(false, true);
        Assertions.assertEquals(List.of(), multiSelectEvents);
    }

    @Test
    void bindValue_writeCallbackRefusesClientSelectAll_selectionAndCheckboxReverted() {
        var model = bindRefusingMultiSelect(Set.of("foo"));

        model.clientSelectAll();

        Assertions.assertEquals(Set.of("foo"), grid.asMultiSelect().getValue());
        assertSelectAllCheckboxState(false, true);
        Assertions.assertEquals(List.of(), multiSelectEvents);
    }

    @Test
    void bindValue_writeCallbackRefusesDeselectAll_selectionAndCheckboxReverted() {
        var model = bindRefusingMultiSelect(Set.of("foo", "bar", "baz"));

        model.deselectAll();

        Assertions.assertEquals(Set.of("foo", "bar", "baz"),
                grid.asMultiSelect().getValue());
        assertSelectAllCheckboxState(true, false);
        Assertions.assertEquals(List.of(), multiSelectEvents);
    }

    @Test
    void bindValue_writeCallbackRefusesClientDeselectAll_selectionAndCheckboxReverted() {
        var model = bindRefusingMultiSelect(Set.of("foo", "bar", "baz"));

        model.clientDeselectAll();

        Assertions.assertEquals(Set.of("foo", "bar", "baz"),
                grid.asMultiSelect().getValue());
        assertSelectAllCheckboxState(true, false);
        Assertions.assertEquals(List.of(), multiSelectEvents);
    }

    @Test
    void bindValue_writeCallbackRefusesDeselect_selectionAndCheckboxReverted() {
        var model = bindRefusingMultiSelect(Set.of("foo", "bar", "baz"));

        model.deselect("foo");

        Assertions.assertEquals(Set.of("foo", "bar", "baz"),
                grid.asMultiSelect().getValue());
        assertSelectAllCheckboxState(true, false);
        Assertions.assertEquals(List.of(), multiSelectEvents);
    }

    private AbstractGridMultiSelectionModel<String> bindRefusingMultiSelect(
            Set<String> initialValue) {
        grid.setItems("foo", "bar", "baz");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.asMultiSelect().bindValue(new ValueSignal<>(initialValue), v -> {
        });
        UI.getCurrent().add(grid);
        grid.asMultiSelect().addValueChangeListener(
                e -> multiSelectEvents.add(e.getValue()));
        return (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();
    }

    private void assertSelectAllCheckboxState(boolean selectAll,
            boolean indeterminate) {
        var column = ((AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel()).getSelectionColumn();
        Assertions.assertEquals(selectAll,
                column.getElement().getProperty("selectAll", false));
        Assertions.assertEquals(indeterminate,
                column.getElement().getProperty("_indeterminate", false));
    }

    @Test
    void bindValue_switchToSingle_bindingCleanedUp() {
        grid.setItems("foo", "bar", "baz");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var signal = new ValueSignal<>(Set.of("foo", "bar"));
        grid.asMultiSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assertions.assertEquals(Set.of("foo", "bar"),
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
        Assertions.assertFalse(eventFired[0],
                "Old signal effect should not fire after model switch");
        Assertions.assertTrue(grid.asMultiSelect().getValue().isEmpty());
    }

    @Test
    void bindValue_switchToSingle_canBindNewSignal() {
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

        Assertions.assertEquals("bar", grid.asSingleSelect().getValue());
    }
}
