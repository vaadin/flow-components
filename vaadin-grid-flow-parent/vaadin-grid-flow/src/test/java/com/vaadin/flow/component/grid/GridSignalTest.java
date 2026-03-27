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

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class GridSignalTest extends AbstractSignalsTest {

    private Grid<String> grid;

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

    // ===== SINGLE SELECT BIND VALUE TESTS =====

    @Test
    void singleSelect_bindValue_signalSetsSelection() {
        grid.setItems("foo", "bar", "baz");
        var signal = new ValueSignal<>("foo");
        grid.asSingleSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assertions.assertEquals("foo", grid.asSingleSelect().getValue());

        signal.set("bar");
        Assertions.assertEquals("bar", grid.asSingleSelect().getValue());
    }

    @Test
    void singleSelect_bindValue_clientSelectionUpdatesSignal() {
        grid.setItems("foo", "bar", "baz");
        var signal = new ValueSignal<String>(null);
        grid.asSingleSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assertions.assertNull(signal.peek());

        ((GridSingleSelectionModel<String>) grid.getSelectionModel())
                .selectFromClient("bar");

        Assertions.assertEquals("bar", signal.peek());
    }

    @Test
    void singleSelect_bindValue_bindAgainWhileBound_throwsException() {
        grid.setItems("foo", "bar");
        var signal = new ValueSignal<String>(null);
        grid.asSingleSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assertions.assertThrows(BindingActiveException.class, () -> grid
                .asSingleSelect().bindValue(new ValueSignal<>("foo"), v -> {
                }));
    }

    @Test
    void singleSelect_bindValue_noEffectWhenDetached() {
        grid.setItems("foo", "bar", "baz");
        var signal = new ValueSignal<>("foo");
        grid.asSingleSelect().bindValue(signal, signal::set);
        // Not attached — initial probe sets "foo", then effect is passivated

        // Subsequent signal changes while detached should have no effect
        signal.set("bar");
        Assertions.assertEquals("foo", grid.asSingleSelect().getValue());
    }

    @Test
    void singleSelect_bindValue_signalChangeDoesNotInvokeWriteCallback() {
        grid.setItems("foo", "bar", "baz");
        int[] callCount = { 0 };
        var signal = new ValueSignal<String>(null);
        grid.asSingleSelect().bindValue(signal, v -> callCount[0]++);
        UI.getCurrent().add(grid);

        // Signal-originated change should not trigger the write callback
        signal.set("bar");
        Assertions.assertEquals("bar", grid.asSingleSelect().getValue());
        Assertions.assertEquals(0, callCount[0]);

        // Client-originated change should trigger the write callback
        ((GridSingleSelectionModel<String>) grid.getSelectionModel())
                .selectFromClient("baz");
        Assertions.assertEquals(1, callCount[0]);
    }

    // ===== MULTI SELECT BIND VALUE TESTS =====

    @Test
    void multiSelect_bindValue_signalSetsSelection() {
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
    void multiSelect_bindValue_clientSelectionUpdatesSignal() {
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
    void multiSelect_bindValue_bindAgainWhileBound_throwsException() {
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
    void multiSelect_bindValue_signalChangeDoesNotInvokeWriteCallback() {
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

    // ===== SELECTION MODEL SWITCH CLEANUP TESTS =====

    @Test
    void singleSelect_bindValue_switchToMulti_bindingCleanedUp() {
        grid.setItems("foo", "bar", "baz");
        var signal = new ValueSignal<>("foo");
        grid.asSingleSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assertions.assertEquals("foo", grid.asSingleSelect().getValue());

        // Switch to multi-select; the old binding should be cleaned up
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        // Switch back to single-select so SingleSelectionEvent is active again
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        // Track whether a spurious selection event fires from the old effect
        boolean[] eventFired = { false };
        grid.asSingleSelect().addValueChangeListener(e -> eventFired[0] = true);

        // Changing the old signal should not trigger the old effect
        signal.set("bar");
        Assertions.assertFalse(eventFired[0],
                "Old signal effect should not fire after model switch");
        Assertions.assertNull(grid.asSingleSelect().getValue());
    }

    @Test
    void singleSelect_bindValue_switchToMulti_canBindNewSignal() {
        grid.setItems("foo", "bar", "baz");
        var signal = new ValueSignal<>("foo");
        grid.asSingleSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        // Switch to multi-select
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        // Should be able to bind a new signal without BindingActiveException
        var multiSignal = new ValueSignal<>(Set.of("bar"));
        grid.asMultiSelect().bindValue(multiSignal, multiSignal::set);

        Assertions.assertEquals(Set.of("bar"), grid.asMultiSelect().getValue());
    }

    @Test
    void multiSelect_bindValue_switchToSingle_bindingCleanedUp() {
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
    void multiSelect_bindValue_switchToSingle_canBindNewSignal() {
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
