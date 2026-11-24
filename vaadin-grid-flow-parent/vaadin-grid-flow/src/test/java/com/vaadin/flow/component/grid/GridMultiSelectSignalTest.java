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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.flow.internal.nodefeature.PropertyChangeDeniedException;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.flow.signals.shared.SharedValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

import tools.jackson.core.type.TypeReference;

class GridMultiSelectSignalTest extends AbstractSignalsTest {

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

    @Nested
    class WriteCallbackRefuses {

        private ValueSignal<Set<String>> signal;
        private AbstractGridMultiSelectionModel<String> model;
        private final List<Set<String>> events = new ArrayList<>();

        @BeforeEach
        void setup() {
            grid.setItems("foo", "bar", "baz");
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
            signal = new ValueSignal<>(Set.of("foo"));
            grid.asMultiSelect().bindValue(signal, v -> {
            });
            UI.getCurrent().add(grid);
            grid.asMultiSelect()
                    .addValueChangeListener(e -> events.add(e.getValue()));
            model = (AbstractGridMultiSelectionModel<String>) grid
                    .getSelectionModel();
        }

        @Test
        void selectFromClient_selectionRevertedWithoutEvent() {
            model.selectFromClient("bar");
            assertSelectionState(Set.of("foo"));
            assertCheckboxState(false, true);
            Assertions.assertEquals(0, events.size());
        }

        @Test
        void select_selectionRevertedWithoutEvent() {
            model.select("bar");
            assertSelectionState(Set.of("foo"));
            assertCheckboxState(false, true);
            Assertions.assertEquals(0, events.size());
        }

        @Test
        void deselect_selectionAndCheckboxReverted() {
            signal.set(Set.of("foo", "bar", "baz"));
            events.clear();

            model.deselect("foo");
            assertSelectionState(Set.of("foo", "bar", "baz"));
            assertCheckboxState(true, false);
            Assertions.assertEquals(0, events.size());
        }

        @Test
        void selectAll_selectionAndCheckboxReverted() {
            model.selectAll();
            assertSelectionState(Set.of("foo"));
            assertCheckboxState(false, true);
            Assertions.assertEquals(0, events.size());
        }

        @Test
        void clientSelectAll_selectionAndCheckboxReverted() throws Exception {
            setSelectAllFromClient(true);
            model.clientSelectAll();
            assertSelectionState(Set.of("foo"));
            assertCheckboxState(false, true);
            Assertions.assertEquals(0, events.size());
        }

        @Test
        void deselectAll_selectionAndCheckboxReverted() {
            model.deselectAll();
            assertSelectionState(Set.of("foo"));
            assertCheckboxState(false, true);
            Assertions.assertEquals(0, events.size());
        }

        @Test
        void clientDeselectAll_selectionAndCheckboxReverted() throws Exception {
            signal.set(Set.of("foo", "bar", "baz"));
            events.clear();

            setSelectAllFromClient(false);
            model.clientDeselectAll();
            assertSelectionState(Set.of("foo", "bar", "baz"));
            assertCheckboxState(true, false);
            Assertions.assertEquals(0, events.size());
        }

        @Test
        void deselectFromClient_selectionAndCheckboxReverted()
                throws Exception {
            signal.set(Set.of("foo", "bar", "baz"));
            events.clear();

            setSelectAllFromClient(false);
            model.deselectFromClient("foo");
            assertSelectionState(Set.of("foo", "bar", "baz"));
            assertCheckboxState(true, false);
            Assertions.assertEquals(0, events.size());
        }

        private void setSelectAllFromClient(boolean selectAll)
                throws PropertyChangeDeniedException {
            model.getSelectionColumn().getElement().getNode()
                    .getFeature(ElementPropertyMap.class)
                    .deferredUpdateFromClient("selectAll", selectAll).run();
        }

        private void assertSelectionState(Set<String> selection) {
            Assertions.assertEquals(selection, grid.asMultiSelect().getValue());
            Assertions.assertEquals(selection, signal.peek());
        }

        private void assertCheckboxState(boolean selectAll,
                boolean indeterminate) {
            var column = model.getSelectionColumn();
            Assertions.assertEquals(selectAll,
                    column.getElement().getProperty("selectAll", false));
            Assertions.assertEquals(indeterminate,
                    column.getElement().getProperty("_indeterminate", false));
        }
    }

    @Test
    void bindValue_writeCallbackStoresOtherSelectionInTransaction_sendsStoredSelectionToClient() {
        Set<String> clientSelected = new HashSet<>();
        Set<String> clientDeselected = new HashSet<>();
        grid = new Grid<>() {
            @Override
            void doClientSideSelection(Set<String> items) {
                clientSelected.addAll(items);
            }

            @Override
            void doClientSideDeselection(Set<String> items) {
                clientDeselected.addAll(items);
            }

            @Override
            boolean isInActiveRange(String item) {
                return true;
            }
        };
        grid.setItems("foo", "bar", "baz");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var signal = new SharedValueSignal<Set<String>>(Set.of("foo"),
                new TypeReference<>() {
                });
        grid.asMultiSelect().bindValue(signal,
                value -> signal.set(Set.of("bar", "baz")));
        UI.getCurrent().add(grid);
        clientSelected.clear();
        clientDeselected.clear();

        Signal.runInTransaction(() -> grid.select("bar"));

        Assertions.assertEquals(Set.of("bar", "baz"), grid.getSelectedItems());
        Assertions.assertEquals(Set.of("bar", "baz"), clientSelected);
        Assertions.assertEquals(Set.of("foo"), clientDeselected);
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
