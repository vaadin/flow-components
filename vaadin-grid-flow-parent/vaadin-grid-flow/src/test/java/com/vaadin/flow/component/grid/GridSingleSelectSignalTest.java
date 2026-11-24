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

class GridSingleSelectSignalTest extends AbstractSignalsTest {

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
        var signal = new ValueSignal<>("foo");
        grid.asSingleSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assertions.assertEquals("foo", grid.asSingleSelect().getValue());

        signal.set("bar");
        Assertions.assertEquals("bar", grid.asSingleSelect().getValue());
    }

    @Test
    void bindValue_clientSelectionUpdatesSignal() {
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
    void bindValue_bindAgainWhileBound_throwsException() {
        grid.setItems("foo", "bar");
        var signal = new ValueSignal<String>(null);
        grid.asSingleSelect().bindValue(signal, signal::set);
        UI.getCurrent().add(grid);

        Assertions.assertThrows(BindingActiveException.class, () -> grid
                .asSingleSelect().bindValue(new ValueSignal<>("foo"), v -> {
                }));
    }

    @Test
    void bindValue_nullWriteCallback_throwsException() {
        grid.setItems("foo", "bar");
        var signal = new ValueSignal<String>(null);

        Assertions.assertThrows(NullPointerException.class,
                () -> grid.asSingleSelect().bindValue(signal, null));
    }

    @Test
    void bindValue_noEffectWhenDetached() {
        grid.setItems("foo", "bar", "baz");
        var signal = new ValueSignal<>("foo");
        grid.asSingleSelect().bindValue(signal, signal::set);
        // Not attached — initial probe sets "foo", then effect is passivated

        // Subsequent signal changes while detached should have no effect
        signal.set("bar");
        Assertions.assertEquals("foo", grid.asSingleSelect().getValue());
    }

    @Test
    void bindValue_signalChangeDoesNotInvokeWriteCallback() {
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

    @Test
    void bindValue_writeCallbackRefuses_selectionRevertedWithoutEvent() {
        grid.setItems("foo", "bar", "baz");
        var signal = new ValueSignal<>("foo");
        grid.asSingleSelect().bindValue(signal, v -> {
        });
        UI.getCurrent().add(grid);
        List<String> events = new ArrayList<>();
        grid.asSingleSelect()
                .addValueChangeListener(e -> events.add(e.getValue()));

        ((GridSingleSelectionModel<String>) grid.getSelectionModel())
                .selectFromClient("bar");
        Assertions.assertEquals("foo", grid.asSingleSelect().getValue());

        grid.select("baz");
        Assertions.assertEquals("foo", grid.asSingleSelect().getValue());

        Assertions.assertEquals("foo", signal.peek());
        Assertions.assertEquals(List.of(), events);
    }

    @Test
    void bindValue_switchToMulti_bindingCleanedUp() {
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
    void bindValue_switchToMulti_canBindNewSignal() {
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
}
