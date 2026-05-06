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
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/focus-selected-item")
public class ComboBoxFocusSelectedItemPage extends VerticalLayout {

    private static final int ITEM_COUNT = 10_000;

    private final List<String> allItems = IntStream.range(0, ITEM_COUNT)
            .mapToObj(i -> "Item " + i).toList();

    public ComboBoxFocusSelectedItemPage() {
        addMainLazySection();
        addSmallInMemorySection();
        addDefaultOffSection();
        addNoProviderSection();
        addThrowingProviderSection();
        addDomainTypeSection();
        addBinderSection();
    }

    private void addMainLazySection() {
        ComboBox<String> comboBox = new ComboBox<>("Lazy combo-box");
        comboBox.setId("combo");
        comboBox.setWidth("16em");

        setLazyItems(comboBox);
        // ItemIndexProvider resolves the flat index of an item *within the
        // currently filtered list*. The query carries the filter so the
        // index stays accurate while the user is typing.
        setFilteredIndexProvider(comboBox);

        comboBox.setFocusSelectedItem(true);
        comboBox.setValue("Item 5000");

        Span status = new Span();
        status.setId("status");
        updateStatus(status, comboBox);
        comboBox.addValueChangeListener(e -> updateStatus(status, comboBox));

        NativeButton setDeep = button("set-deep", "Select Item 9000",
                () -> comboBox.setValue("Item 9000"));
        NativeButton setShallow = button("set-shallow", "Select Item 123",
                () -> comboBox.setValue("Item 123"));
        NativeButton clear = button("clear", "Clear value", comboBox::clear);
        NativeButton toggle = button("toggle", "Toggle focusSelectedItem",
                () -> {
                    comboBox.setFocusSelectedItem(
                            !comboBox.isFocusSelectedItem());
                    updateStatus(status, comboBox);
                });

        // Push-style server-side value change while the dropdown may still be
        // open (C3). In a real app this would come from a background thread or
        // a collaborative event; clicking the button is equivalent from the
        // connector's perspective.
        NativeButton pushUpdate = button("push-update",
                "Push value = Item 7500", () -> comboBox.setValue("Item 7500"));

        // Detach/reattach the combo (C4). Remove it from the layout and add it
        // back to verify the connector still works after reattachment.
        NativeButton detachReattach = button("detach-reattach",
                "Detach & reattach", () -> {
                    remove(comboBox);
                    add(comboBox);
                });

        // Replace the backing data provider with a list that does NOT contain
        // the current value (C6). The combo retains its `value` but
        // `selectedItem` should either clear or not resolve; opening the
        // dropdown must not crash and must not land focus on an unrelated row.
        NativeButton replaceItems = button("replace-items",
                "Replace items (drops current value)",
                () -> comboBox.setItems("Alpha", "Beta", "Gamma"));

        add(new H4("Lazy combo-box"), comboBox, status,
                new HorizontalLayout(setDeep, setShallow, clear, toggle),
                new HorizontalLayout(pushUpdate, detachReattach, replaceItems));
    }

    private void addSmallInMemorySection() {
        // Small in-memory combo-box: itemCount <= pageSize activates
        // _clientSideFilter mode, where the server never sees the typed
        // filter. Exercises the resolveSelectedItemIndex client-side-filter
        // guard: after filtering out the selected item, reopening must not
        // scroll to the unfiltered position.
        ComboBox<String> smallCombo = new ComboBox<>("Small (client-filter)");
        smallCombo.setId("small-combo");
        smallCombo.setItems("apple", "banana", "cherry", "date", "elderberry");
        smallCombo.setFocusSelectedItem(true);
        smallCombo.setValue("elderberry");
        add(new H4("Small in-memory combo-box"), smallCombo);
    }

    private void addDefaultOffSection() {
        // A1: focusSelectedItem defaults to false. Opening this combo with a
        // preset value must not auto-scroll (regression guard for PR #6055).
        ComboBox<String> combo = new ComboBox<>("Default off");
        combo.setId("combo-default");
        setLazyItems(combo);
        combo.setValue("Item 200");
        add(new H4("Default off (focusSelectedItem unset)"), combo);
    }

    private void addNoProviderSection() {
        // C8 / C9: lazy combo with focusSelectedItem=true but NO
        // ItemIndexProvider. Falls back to the web-component's client-side
        // best-effort: focuses only if the selected item is in the loaded
        // cache.
        ComboBox<String> combo = new ComboBox<>("Lazy (no ItemIndexProvider)");
        combo.setId("combo-no-provider");
        setLazyItems(combo);
        combo.setFocusSelectedItem(true);

        NativeButton near = button("set-no-provider-near",
                "Select Item 5 (in first page)",
                () -> combo.setValue("Item 5"));
        NativeButton far = button("set-no-provider-far",
                "Select Item 500 (not in first page)",
                () -> combo.setValue("Item 500"));

        add(new H4("Lazy without ItemIndexProvider"), combo,
                new HorizontalLayout(near, far));
    }

    private void addThrowingProviderSection() {
        // C7: ItemIndexProvider that throws a RuntimeException when armed.
        // Opening the dropdown with the throw enabled must not crash or leave
        // the combo in a broken state; the feature should silently fall back.
        boolean[] throwOnResolve = { false };
        ComboBox<String> combo = new ComboBox<>("Lazy (provider throws)");
        combo.setId("combo-throws");
        setLazyItems(combo);
        combo.getLazyDataView().setItemIndexProvider((item, query) -> {
            if (throwOnResolve[0]) {
                throw new RuntimeException(
                        "Simulated ItemIndexProvider failure");
            }
            String filterText = query.getFilter().map(Object::toString)
                    .orElse("");
            return resolveItemIndex(item, filterText);
        });
        combo.setFocusSelectedItem(true);
        combo.setValue("Item 300");

        NativeButton toggleThrow = button("toggle-throw",
                "Toggle provider-throws mode",
                () -> throwOnResolve[0] = !throwOnResolve[0]);

        add(new H4("Lazy with throwing ItemIndexProvider"), combo, toggleThrow);
    }

    private void addDomainTypeSection() {
        // C2: custom domain type with ItemLabelGenerator + IdentifierProvider.
        // Id-based identity ensures the preset value is found even if a
        // re-fetched Person is a different object reference.
        List<Person> persons = IntStream.range(0, 100)
                .mapToObj(i -> new Person(i, "Person " + i)).toList();
        ComboBox<Person> combo = new ComboBox<>("Persons");
        combo.setId("combo-person");
        combo.setItems(persons);
        combo.setItemLabelGenerator(Person::name);
        combo.getListDataView().setIdentifierProvider(Person::id);
        combo.setFocusSelectedItem(true);
        combo.setValue(new Person(42, "Person 42"));
        add(new H4("Domain type (Person)"), combo);
    }

    private void addBinderSection() {
        // C1: combo wired to a Binder-managed bean. Tests that value changes
        // propagated via Binder go through the same code path.
        Bean bean = new Bean();
        ComboBox<String> combo = new ComboBox<>("Bound to Binder");
        combo.setId("combo-bound");
        setLazyItems(combo);
        setFilteredIndexProvider(combo);
        combo.setFocusSelectedItem(true);

        Binder<Bean> binder = new Binder<>(Bean.class);
        binder.forField(combo).bind("itemName");
        binder.setBean(bean);

        NativeButton updateBean = button("update-bean", "Bean → Item 2500",
                () -> {
                    bean.setItemName("Item 2500");
                    binder.readBean(bean);
                });

        add(new H4("Binder-wired combo"), combo,
                new HorizontalLayout(updateBean));
    }

    private void setLazyItems(ComboBox<String> comboBox) {
        comboBox.setItems(
                query -> filter(query.getFilter().orElse(""))
                        .skip(query.getOffset()).limit(query.getLimit()),
                query -> (int) filter(query.getFilter().orElse("")).count());
    }

    private void setFilteredIndexProvider(ComboBox<String> comboBox) {
        comboBox.getLazyDataView().setItemIndexProvider((item, query) -> {
            String filterText = query.getFilter().map(Object::toString)
                    .orElse("");
            return resolveItemIndex(item, filterText);
        });
    }

    private Integer resolveItemIndex(String item, String filterText) {
        int index = filter(filterText).toList().indexOf(item);
        return index >= 0 ? index : null;
    }

    private Stream<String> filter(String filter) {
        if (filter == null || filter.isEmpty()) {
            return allItems.stream();
        }
        String lower = filter.toLowerCase();
        return allItems.stream()
                .filter(item -> item.toLowerCase().contains(lower));
    }

    private void updateStatus(Span status, ComboBox<String> comboBox) {
        status.setText("value=" + comboBox.getValue() + ", focusSelectedItem="
                + comboBox.isFocusSelectedItem());
    }

    private NativeButton button(String id, String text, Runnable action) {
        NativeButton b = new NativeButton(text, e -> action.run());
        b.setId(id);
        return b;
    }

    public record Person(int id, String name) {
    }

    public static class Bean {
        private String itemName;

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Bean b && Objects.equals(itemName, b.itemName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(itemName);
        }
    }
}
