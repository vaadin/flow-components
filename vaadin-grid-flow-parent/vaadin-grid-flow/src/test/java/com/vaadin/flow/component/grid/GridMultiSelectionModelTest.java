/*
 * Copyright 2000-2022 Vaadin Ltd.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.data.selection.SelectionEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GridMultiSelectionModelTest {

    private static final Person PERSON_C = new Person("c", 3);
    private static final Person PERSON_B = new Person("b", 2);
    private static final Person PERSON_A = new Person("a", 1);

    private Grid<Person> grid;
    private GridMultiSelectionModel<Person> selectionModel;
    private AtomicReference<Set<Person>> currentSelectionCapture;
    private AtomicReference<Set<Person>> oldSelectionCapture;
    private AtomicInteger events;

    @Before
    public void setUp() {
        grid = new Grid<>();
        selectionModel = (GridMultiSelectionModel<Person>) grid
                .setSelectionMode(SelectionMode.MULTI);

        grid.setItems(PERSON_A, PERSON_B, PERSON_C);

        currentSelectionCapture = new AtomicReference<>();
        oldSelectionCapture = new AtomicReference<>();
        events = new AtomicInteger();

        selectionModel.addMultiSelectionListener(event -> {
            currentSelectionCapture.set(new HashSet<>(event.getValue()));
            oldSelectionCapture.set(new HashSet<>(event.getOldSelection()));
            events.incrementAndGet();
        });
    }

    @Test(expected = IllegalStateException.class)
    public void selectionModelChanged_usingPreviousSelectionModel_throws() {
        grid.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(PERSON_A);
    }

    @Test
    public void changingSelectionModel_firesSelectionEvent() {
        Grid<String> customGrid = new Grid<>();
        customGrid.setSelectionMode(SelectionMode.MULTI);
        customGrid.setItems("Foo", "Bar", "Baz");

        Set<String> selectionChanges = new HashSet<>();
        AtomicReference<Set<String>> oldSelectionCapture = new AtomicReference<>();
        ((GridMultiSelectionModel<String>) customGrid.getSelectionModel())
                .addMultiSelectionListener(e -> {
                    selectionChanges.addAll(e.getValue());
                    oldSelectionCapture.set(new HashSet<>(e.getOldSelection()));
                });

        customGrid.getSelectionModel().select("Foo");
        assertEquals(asSet("Foo"), selectionChanges);
        selectionChanges.clear();

        customGrid.getSelectionModel().select("Bar");
        assertEquals("Foo",
                customGrid.getSelectionModel().getFirstSelectedItem().get());
        assertEquals(asSet("Bar", "Foo"), selectionChanges);
        selectionChanges.clear();

        customGrid.setSelectionMode(SelectionMode.SINGLE);
        assertFalse(customGrid.getSelectionModel().getFirstSelectedItem()
                .isPresent());
        assertEquals(Collections.emptySet(), selectionChanges);
        assertEquals(asSet("Bar", "Foo"), oldSelectionCapture.get());
    }

    @Test
    public void select_gridWithStrings() {
        Grid<String> gridWithStrings = new Grid<>();
        gridWithStrings.setSelectionMode(SelectionMode.MULTI);
        gridWithStrings.setItems("Foo", "Bar", "Baz");

        GridSelectionModel<String> model = gridWithStrings.getSelectionModel();
        assertFalse(model.isSelected("Foo"));

        model.select("Foo");
        assertTrue(model.isSelected("Foo"));
        assertEquals(Optional.of("Foo"), model.getFirstSelectedItem());

        model.select("Bar");
        assertTrue(model.isSelected("Foo"));
        assertTrue(model.isSelected("Bar"));
        assertEquals(asSet("Bar", "Foo"), model.getSelectedItems());

        model.deselect("Bar");
        assertFalse(model.isSelected("Bar"));
        assertTrue(model.getFirstSelectedItem().isPresent());
        assertEquals(asSet("Foo"), model.getSelectedItems());
    }

    @Test
    public void select() {
        selectionModel.select(PERSON_B);

        assertEquals(PERSON_B,
                selectionModel.getFirstSelectedItem().orElse(null));
        assertEquals(Optional.of(PERSON_B),
                selectionModel.getFirstSelectedItem());

        assertFalse(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertFalse(selectionModel.isSelected(PERSON_C));

        assertEquals(asSet(PERSON_B), currentSelectionCapture.get());

        selectionModel.select(PERSON_A);
        assertEquals(PERSON_B,
                selectionModel.getFirstSelectedItem().orElse(null));

        assertTrue(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertFalse(selectionModel.isSelected(PERSON_C));

        assertEquals(asSet(PERSON_A, PERSON_B), currentSelectionCapture.get());
        assertEquals(2, events.get());
    }

    @Test
    public void deselect() {
        selectionModel.select(PERSON_B);
        selectionModel.deselect(PERSON_B);

        assertFalse(selectionModel.getFirstSelectedItem().isPresent());

        assertFalse(selectionModel.isSelected(PERSON_A));
        assertFalse(selectionModel.isSelected(PERSON_B));
        assertFalse(selectionModel.isSelected(PERSON_C));

        assertEquals(Collections.emptySet(), currentSelectionCapture.get());
        assertEquals(2, events.get());
    }

    @Test
    public void selectItems() {
        selectionModel.selectItems(PERSON_C, PERSON_B);

        assertEquals(PERSON_C,
                selectionModel.getFirstSelectedItem().orElse(null));
        assertEquals(Optional.of(PERSON_C),
                selectionModel.getFirstSelectedItem());

        assertFalse(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertTrue(selectionModel.isSelected(PERSON_C));

        assertEquals(asSet(PERSON_C, PERSON_B), currentSelectionCapture.get());

        selectionModel.selectItems(PERSON_A, PERSON_C); // partly NOOP
        assertTrue(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertTrue(selectionModel.isSelected(PERSON_C));

        assertEquals(asSet(PERSON_C, PERSON_A, PERSON_B),
                currentSelectionCapture.get());
        assertEquals(2, events.get());
    }

    @Test
    public void deselectItems() {
        selectionModel.selectItems(PERSON_C, PERSON_A, PERSON_B);

        selectionModel.deselectItems(PERSON_A);
        assertEquals(PERSON_C,
                selectionModel.getFirstSelectedItem().orElse(null));
        assertEquals(Optional.of(PERSON_C),
                selectionModel.getFirstSelectedItem());

        assertFalse(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertTrue(selectionModel.isSelected(PERSON_C));

        assertEquals(asSet(PERSON_C, PERSON_B), currentSelectionCapture.get());

        selectionModel.deselectItems(PERSON_A, PERSON_B, PERSON_C);
        assertNull(selectionModel.getFirstSelectedItem().orElse(null));
        assertEquals(Optional.empty(), selectionModel.getFirstSelectedItem());

        assertFalse(selectionModel.isSelected(PERSON_A));
        assertFalse(selectionModel.isSelected(PERSON_B));
        assertFalse(selectionModel.isSelected(PERSON_C));

        assertEquals(Collections.emptySet(), currentSelectionCapture.get());
        assertEquals(3, events.get());
    }

    @Test
    public void selectionEvent_newSelection_oldSelection() {
        selectionModel.selectItems(PERSON_C, PERSON_A, PERSON_B);

        assertEquals(asSet(PERSON_C, PERSON_A, PERSON_B),
                currentSelectionCapture.get());
        assertEquals(Collections.emptySet(), oldSelectionCapture.get());

        selectionModel.deselect(PERSON_A);

        assertEquals(asSet(PERSON_C, PERSON_B), currentSelectionCapture.get());
        assertEquals(asSet(PERSON_C, PERSON_A, PERSON_B),
                oldSelectionCapture.get());

        selectionModel.deselectItems(PERSON_A, PERSON_B, PERSON_C);
        assertEquals(Collections.emptySet(), currentSelectionCapture.get());
        assertEquals(asSet(PERSON_C, PERSON_B), oldSelectionCapture.get());

        selectionModel.selectItems(PERSON_A);
        assertEquals(asSet(PERSON_A), currentSelectionCapture.get());
        assertEquals(Collections.emptySet(), oldSelectionCapture.get());

        selectionModel.updateSelection(
                new LinkedHashSet<>(asSet(PERSON_C, PERSON_B)),
                new LinkedHashSet<>(asSet(PERSON_A)));
        assertEquals(asSet(PERSON_C, PERSON_B), currentSelectionCapture.get());
        assertEquals(asSet(PERSON_A), oldSelectionCapture.get());

        selectionModel.deselectAll();
        assertEquals(Collections.emptySet(), currentSelectionCapture.get());
        assertEquals(asSet(PERSON_C, PERSON_B), oldSelectionCapture.get());

        selectionModel.select(PERSON_C);
        assertEquals(asSet(PERSON_C), currentSelectionCapture.get());
        assertEquals(Collections.emptySet(), oldSelectionCapture.get());

        selectionModel.deselect(PERSON_C);
        assertEquals(Collections.emptySet(), currentSelectionCapture.get());
        assertEquals(asSet(PERSON_C), oldSelectionCapture.get());
    }

    @Test
    public void deselectAll() {
        selectionModel.selectItems(PERSON_A, PERSON_C, PERSON_B);

        assertTrue(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertTrue(selectionModel.isSelected(PERSON_C));
        assertEquals(asSet(PERSON_C, PERSON_A, PERSON_B),
                currentSelectionCapture.get());
        assertEquals(1, events.get());

        selectionModel.deselectAll();
        assertFalse(selectionModel.isSelected(PERSON_A));
        assertFalse(selectionModel.isSelected(PERSON_B));
        assertFalse(selectionModel.isSelected(PERSON_C));
        assertEquals(Collections.emptySet(), currentSelectionCapture.get());
        assertEquals(asSet(PERSON_C, PERSON_A, PERSON_B),
                oldSelectionCapture.get());
        assertEquals(2, events.get());

        selectionModel.select(PERSON_C);
        assertFalse(selectionModel.isSelected(PERSON_A));
        assertFalse(selectionModel.isSelected(PERSON_B));
        assertTrue(selectionModel.isSelected(PERSON_C));
        assertEquals(asSet(PERSON_C), currentSelectionCapture.get());
        assertEquals(Collections.emptySet(), oldSelectionCapture.get());
        assertEquals(3, events.get());

        selectionModel.deselectAll();
        assertFalse(selectionModel.isSelected(PERSON_A));
        assertFalse(selectionModel.isSelected(PERSON_B));
        assertFalse(selectionModel.isSelected(PERSON_C));
        assertEquals(Collections.emptySet(), currentSelectionCapture.get());
        assertEquals(asSet(PERSON_C), oldSelectionCapture.get());
        assertEquals(4, events.get());

        selectionModel.deselectAll();
        assertEquals(4, events.get());
    }

    @Test
    @Ignore
    // Ignored because selectAll is not implemented yet
    // See https://github.com/vaadin/flow/issues/2546
    public void selectAll() {
        selectionModel.selectAll();

        assertTrue(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertTrue(selectionModel.isSelected(PERSON_C));
        assertEquals(asSet(PERSON_A, PERSON_B, PERSON_C),
                currentSelectionCapture.get());
        assertEquals(1, events.get());

        selectionModel.deselectItems(PERSON_A, PERSON_C);

        assertFalse(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertFalse(selectionModel.isSelected(PERSON_C));
        assertEquals(asSet(PERSON_A, PERSON_B, PERSON_C),
                oldSelectionCapture.get());

        selectionModel.selectAll();

        assertTrue(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertTrue(selectionModel.isSelected(PERSON_C));
        assertEquals(asSet(PERSON_B, PERSON_A, PERSON_C),
                currentSelectionCapture.get());
        assertEquals(asSet(PERSON_B), oldSelectionCapture.get());
        assertEquals(3, events.get());
    }

    @Test
    public void updateSelection() {
        selectionModel.updateSelection(asSet(PERSON_A), Collections.emptySet());

        assertTrue(selectionModel.isSelected(PERSON_A));
        assertFalse(selectionModel.isSelected(PERSON_B));
        assertFalse(selectionModel.isSelected(PERSON_C));
        assertEquals(asSet(PERSON_A), currentSelectionCapture.get());
        assertEquals(1, events.get());

        selectionModel.updateSelection(asSet(PERSON_B), asSet(PERSON_A));

        assertFalse(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertFalse(selectionModel.isSelected(PERSON_C));
        assertEquals(asSet(PERSON_B), currentSelectionCapture.get());
        assertEquals(asSet(PERSON_A), oldSelectionCapture.get());
        assertEquals(2, events.get());

        selectionModel.updateSelection(asSet(PERSON_B), asSet(PERSON_A)); // NOOP

        assertFalse(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertFalse(selectionModel.isSelected(PERSON_C));
        assertEquals(asSet(PERSON_B), currentSelectionCapture.get());
        assertEquals(asSet(PERSON_A), oldSelectionCapture.get());
        assertEquals(2, events.get());

        selectionModel.updateSelection(asSet(PERSON_A, PERSON_C),
                asSet(PERSON_A)); // partly NOOP

        assertFalse(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertTrue(selectionModel.isSelected(PERSON_C));
        assertEquals(asSet(PERSON_C, PERSON_B), currentSelectionCapture.get());
        assertEquals(asSet(PERSON_B), oldSelectionCapture.get());
        assertEquals(3, events.get());

        selectionModel.updateSelection(asSet(PERSON_B, PERSON_A),
                asSet(PERSON_B)); // partly NOOP

        assertTrue(selectionModel.isSelected(PERSON_A));
        assertTrue(selectionModel.isSelected(PERSON_B));
        assertTrue(selectionModel.isSelected(PERSON_C));
        assertEquals(asSet(PERSON_C, PERSON_A, PERSON_B),
                currentSelectionCapture.get());
        assertEquals(asSet(PERSON_C, PERSON_B), oldSelectionCapture.get());
        assertEquals(4, events.get());

        selectionModel.updateSelection(asSet(),
                asSet(PERSON_B, PERSON_A, PERSON_C));

        assertFalse(selectionModel.isSelected(PERSON_A));
        assertFalse(selectionModel.isSelected(PERSON_B));
        assertFalse(selectionModel.isSelected(PERSON_C));
        assertEquals(Collections.emptySet(), currentSelectionCapture.get());
        assertEquals(asSet(PERSON_C, PERSON_A, PERSON_B),
                oldSelectionCapture.get());
        assertEquals(5, events.get());
    }

    private <T> Set<T> asSet(@SuppressWarnings("unchecked") T... people) {
        return new LinkedHashSet<>(Arrays.asList(people));
    }

    @Test
    public void selectTwice() {
        selectionModel.select(PERSON_C);
        selectionModel.select(PERSON_C);

        assertEquals(PERSON_C,
                selectionModel.getFirstSelectedItem().orElse(null));

        assertFalse(selectionModel.isSelected(PERSON_A));
        assertFalse(selectionModel.isSelected(PERSON_B));
        assertTrue(selectionModel.isSelected(PERSON_C));

        assertEquals(Optional.of(PERSON_C),
                selectionModel.getFirstSelectedItem());

        assertEquals(asSet(PERSON_C), currentSelectionCapture.get());
        assertEquals(1, events.get());
    }

    @Test
    public void deselectTwice() {
        selectionModel.select(PERSON_C);
        assertEquals(asSet(PERSON_C), currentSelectionCapture.get());
        assertEquals(1, events.get());

        selectionModel.deselect(PERSON_C);

        assertFalse(selectionModel.getFirstSelectedItem().isPresent());
        assertFalse(selectionModel.isSelected(PERSON_C));
        assertEquals(Collections.emptySet(), currentSelectionCapture.get());
        assertEquals(2, events.get());

        selectionModel.deselect(PERSON_C);

        assertFalse(selectionModel.getFirstSelectedItem().isPresent());
        assertFalse(selectionModel.isSelected(PERSON_A));
        assertFalse(selectionModel.isSelected(PERSON_B));
        assertFalse(selectionModel.isSelected(PERSON_C));
        assertEquals(Collections.emptySet(), currentSelectionCapture.get());
        assertEquals(2, events.get());
    }

    @Test
    public void addValueChangeListener() {
        String value = "foo";

        Grid<String> grid = new Grid<>();

        AtomicReference<MultiSelectionEvent<Grid<String>, String>> event = new AtomicReference<>();
        MultiSelectionListener<Grid<String>, String> selectionListener = evt -> {
            assertNull(event.get());
            event.set(evt);
        };

        GridMultiSelectionModel<String> model = new AbstractGridMultiSelectionModel<String>(
                grid) {
            @Override
            protected void fireSelectionEvent(
                    SelectionEvent<Grid<String>, String> event) {
            }
        };
        model = Mockito.spy(model);
        Mockito.when(model.getSelectedItems())
                .thenReturn(new LinkedHashSet<>(Arrays.asList(value)));

        grid.setItems("foo", "bar");

        model.addMultiSelectionListener(selectionListener);

        selectionListener.selectionChange(new MultiSelectionEvent<>(grid,
                model.asMultiSelect(), Collections.emptySet(), true));

        assertEquals(grid, event.get().getSource());
        assertEquals(new LinkedHashSet<>(asSet(value)), event.get().getValue());
        assertTrue(event.get().isFromClient());

        Mockito.verify(model, Mockito.times(1)).getSelectedItems();
    }

    @Test
    public void shouldUseGetIdFromListProviderToAlterSelectionNoEquals() {
        shouldUseGetIdFromListProviderToAlterSelection(NoEquals::new,
                NoEquals::getLabel);
    }

    @Test
    public void shouldUseGetIdFromListProviderToAlterSelectionAllEquals() {
        shouldUseGetIdFromListProviderToAlterSelection(AllEquals::new,
                AllEquals::getLabel);
    }

    private <T> void shouldUseGetIdFromListProviderToAlterSelection(
            Function<String, T> itemFactory, Function<T, String> labelGetter) {
        Grid<T> g = new Grid<T>();
        g.addColumn(labelGetter::apply).setHeader("Label");
        g.setDataProvider(new ListDataProvider<T>(
                Arrays.asList(itemFactory.apply("A"), itemFactory.apply("B"))) {
            @Override
            public Object getId(T item) {
                return labelGetter.apply(item);
            }
        });
        g.setSelectionMode(Grid.SelectionMode.MULTI);
        g.select(itemFactory.apply("B"));
        g.select(itemFactory.apply("B"));
        g.select(itemFactory.apply("B"));

        assertEquals(1, g.getSelectedItems().size());
        g.deselect(itemFactory.apply("A"));
        assertEquals(1, g.getSelectedItems().size());
        g.deselect(itemFactory.apply("B"));
        assertEquals(0, g.getSelectedItems().size());

    }

    public static class NoEquals {
        private String label;

        public NoEquals(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }

    }

    public static class AllEquals {
        private String label;

        public AllEquals(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof AllEquals;
        }

        @Override
        public int hashCode() {
            return 2;
        }
    }

}
