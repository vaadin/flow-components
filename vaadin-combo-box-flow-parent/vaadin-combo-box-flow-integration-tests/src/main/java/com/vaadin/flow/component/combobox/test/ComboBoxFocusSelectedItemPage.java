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
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.dataview.ComboBoxLazyDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/focus-selected-item")
public class ComboBoxFocusSelectedItemPage extends Div {

    private static final int ITEM_COUNT = 10000;
    private static final List<String> ALL_ITEMS = IntStream.range(0, ITEM_COUNT)
            .mapToObj(i -> "Item " + i).collect(Collectors.toList());

    private static final String LAZY_WITH_PROVIDER = "lazy-with-provider";
    private static final String IN_MEMORY = "in-memory";
    private static final String LAZY_TOGGLE_OFF = "lazy-toggle-off";

    public ComboBoxFocusSelectedItemPage() {
        ComboBox<String> withProvider = lazyCombo(LAZY_WITH_PROVIDER);
        withProvider.setFocusSelectedItem(true);
        addSection(
                "Lazy combo with ItemIndexProvider, toggle on, preset Item 5000",
                withProvider, controlsFor(withProvider));
        addSeparator();

        ComboBox<String> inMemory = new ComboBox<>();
        inMemory.setId(IN_MEMORY);
        inMemory.setItems(ALL_ITEMS.subList(0, 100));
        inMemory.setFocusSelectedItem(true);
        inMemory.setValue("Item 30");
        addSection("In-memory combo, toggle on, preset Item 30", inMemory);
        addSeparator();

        ComboBox<String> toggleOff = lazyCombo(LAZY_TOGGLE_OFF);
        addSection(
                "Lazy combo with ItemIndexProvider, toggle OFF, preset Item 5000",
                toggleOff,
                button(LAZY_TOGGLE_OFF + "-toggle-on", "Toggle on",
                        () -> toggleOff.setFocusSelectedItem(true)),
                button(LAZY_TOGGLE_OFF + "-detach-reattach",
                        "Detach + reattach", () -> {
                            toggleOff.setFocusSelectedItem(true);
                            getElement().removeChild(toggleOff.getElement());
                            getElement().appendChild(toggleOff.getElement());
                        }));
    }

    private ComboBox<String> lazyCombo(String id) {
        ComboBox<String> combo = new ComboBox<>();
        combo.setId(id);
        ComboBoxLazyDataView<String> view = combo.setItems(
                query -> filteredItems(query.getFilter().orElse(""))
                        .skip(query.getOffset()).limit(query.getLimit()),
                query -> (int) filteredItems(query.getFilter().orElse(""))
                        .count());
        view.setItemIndexProvider((item, query) -> {
            String filter = query.getFilter().map(Object::toString).orElse("");
            int idx = filteredItems(filter).toList().indexOf(item);
            return idx >= 0 ? idx : null;
        });
        combo.setValue("Item 5000");
        return combo;
    }

    private NativeButton[] controlsFor(ComboBox<String> combo) {
        return new NativeButton[] {
                button(combo.getId().orElseThrow() + "-set-9000",
                        "Set Item 9000", () -> combo.setValue("Item 9000")),
                button(combo.getId().orElseThrow() + "-set-123", "Set Item 123",
                        () -> combo.setValue("Item 123")),
                button(combo.getId().orElseThrow() + "-clear", "Clear value",
                        () -> combo.setValue(null)),
                button(combo.getId().orElseThrow() + "-toggle-off",
                        "Toggle off",
                        () -> combo.setFocusSelectedItem(false)) };
    }

    private static NativeButton button(String id, String label,
            Runnable action) {
        NativeButton b = new NativeButton(label, e -> action.run());
        b.setId(id);
        return b;
    }

    private void addSection(String title, ComboBox<String> combo,
            NativeButton... controls) {
        add(new Paragraph(title));
        add(combo);
        for (NativeButton control : controls) {
            add(control);
        }
    }

    private void addSeparator() {
        Div spacer = new Div();
        spacer.getStyle().set("height", "100px");
        add(spacer);
        getElement().appendChild(new Element("hr"));
    }

    private static Stream<String> filteredItems(String filter) {
        if (filter == null || filter.isEmpty()) {
            return ALL_ITEMS.stream();
        }
        String normalized = filter.toLowerCase(Locale.ROOT);
        return ALL_ITEMS.stream().filter(
                item -> item.toLowerCase(Locale.ROOT).contains(normalized));
    }
}
