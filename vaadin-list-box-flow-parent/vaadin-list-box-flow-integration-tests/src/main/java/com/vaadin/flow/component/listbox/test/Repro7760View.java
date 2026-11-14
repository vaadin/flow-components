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
package com.vaadin.flow.component.listbox.test;

import java.util.Set;
import java.util.TreeSet;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

/**
 * Repro for https://github.com/vaadin/flow-components/issues/7760
 */
@Route("repro-7760")
public class Repro7760View extends Div {

    public Repro7760View() {
        // A: reporter's wrapper, uses MultiSelect#setValue default method
        SelectionList wrapper = new SelectionList();
        wrapper.setId("wrapper");
        Span wrapperValue = new Span();
        wrapperValue.setId("wrapper-value");
        NativeButton wrapperPreset = new NativeButton("Preselect 1, 2, 3",
                e -> {
                    wrapper.getContent().setValue(Set.of("1", "2", "3"));
                    wrapperValue.setText(sorted(wrapper.getSelectedItems()));
                });
        wrapperPreset.setId("wrapper-preset");
        NativeButton wrapperSet = new NativeButton("Set selection to 1, 3",
                e -> {
                    wrapper.setValue(Set.of("1", "3"));
                    wrapperValue.setText(sorted(wrapper.getSelectedItems()));
                });
        wrapperSet.setId("wrapper-set");
        add(new H3("A: wrapper (MultiSelect#setValue default)"), wrapper,
                wrapperPreset, wrapperSet, new Span("Value: "),
                wrapperValue);

        // B: control, MultiSelectListBox#setValue override
        MultiSelectListBox<String> plain = createListBox("plain");
        Span plainValue = new Span();
        plainValue.setId("plain-value");
        plain.addValueChangeListener(
                e -> plainValue.setText(sorted(e.getValue())));
        NativeButton plainPreset = new NativeButton("Preselect 1, 2, 3",
                e -> plain.setValue(Set.of("1", "2", "3")));
        plainPreset.setId("plain-preset");
        NativeButton plainSet = new NativeButton("Set selection to 1, 3",
                e -> plain.setValue(Set.of("1", "3")));
        plainSet.setId("plain-set");
        add(new H3("B: control, MultiSelectListBox#setValue"), plain,
                plainPreset, plainSet, new Span("Value: "), plainValue);

        // C: direct updateSelection with overlapping added/removed sets
        MultiSelectListBox<String> direct = createListBox("direct");
        Span directValue = new Span();
        directValue.setId("direct-value");
        direct.addValueChangeListener(
                e -> directValue.setText(sorted(e.getValue())));
        NativeButton directPreset = new NativeButton("Preselect 1, 2, 3",
                e -> direct.setValue(Set.of("1", "2", "3")));
        directPreset.setId("direct-preset");
        NativeButton directUpdate = new NativeButton(
                "updateSelection(added={1,3}, removed={1,2,3})",
                e -> direct.updateSelection(Set.of("1", "3"),
                        Set.of("1", "2", "3")));
        directUpdate.setId("direct-update");
        add(new H3("C: MultiSelectListBox#updateSelection, overlapping sets"),
                direct, directPreset, directUpdate, new Span("Value: "),
                directValue);
    }

    private static MultiSelectListBox<String> createListBox(String id) {
        MultiSelectListBox<String> listBox = new MultiSelectListBox<>();
        listBox.setItems("1", "2", "3", "4");
        listBox.setId(id);
        return listBox;
    }

    private static String sorted(Set<String> value) {
        return new TreeSet<>(value).toString();
    }

    public static class SelectionList
            extends Composite<MultiSelectListBox<String>>
            implements MultiSelect<SelectionList, String> {

        @Override
        public void updateSelection(Set<String> addedItems,
                Set<String> removedItems) {
            getContent().updateSelection(addedItems, removedItems);
        }

        @Override
        protected MultiSelectListBox<String> initContent() {
            return createListBox("wrapper-content");
        }

        @Override
        public MultiSelectListBox<String> getContent() {
            return super.getContent();
        }

        @Override
        public Set<String> getSelectedItems() {
            return getContent().getSelectedItems();
        }

        @Override
        public Registration addSelectionListener(
                MultiSelectionListener<SelectionList, String> listener) {
            return () -> {
            };
        }

        @Override
        public Registration addValueChangeListener(
                ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<SelectionList, Set<String>>> listener) {
            return () -> {
            };
        }
    }
}
