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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.tests.MockUIExtension;
import com.vaadin.tests.dataprovider.DataProviderListenersTest;

class ListBoxUnitTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private static final String ITEM1 = "1";
    private static final String ITEM2 = "2";

    private ListBox<String> listBox;

    @BeforeEach
    void setup() {
        listBox = new ListBox<>();
        listBox.setItems(ITEM1, ITEM2);
    }

    @Test
    void getValue_returnsNull() {
        Assertions.assertNull(listBox.getValue());
    }

    @Test
    void setValue_getValue_returnsValue() {
        listBox.setValue(ITEM1);
        Assertions.assertEquals(ITEM1, listBox.getValue());
    }

    @Test
    void setValue_changeItemSet_getValue_returnsNull() {
        listBox.setValue(ITEM1);
        listBox.setItems("a");
        Assertions.assertNull(listBox.getValue());
    }

    @Test
    void setItemEnabledProvider_itemDisabled() {
        listBox.setItemEnabledProvider(item -> item != ITEM2);
        assertDisabledItem(0, false);
        assertDisabledItem(1, true);

        listBox.setItems(ITEM2, ITEM1);
        assertDisabledItem(0, true);
        assertDisabledItem(1, false);
    }

    @Test
    void setIdentifierProvider_setItemWithIdentifierOnly_shouldSelectCorrectItem() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        ListBox<CustomItem> listBox = new ListBox<>();
        ListBoxListDataView<CustomItem> listDataView = listBox.setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        listBox.setValue(new CustomItem(1L));

        Assertions.assertNotNull(listBox.getValue());
        Assertions.assertEquals("First", listBox.getValue().getName());

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item not with the reference of existing item, but instead
        // with just the Id:
        listBox.setValue(new CustomItem(2L));

        Assertions.assertNotNull(listBox.getValue());
        Assertions.assertEquals(Long.valueOf(2L), listBox.getValue().getId());
    }

    @Test
    void setIdentifierProvider_setItemWithIdAndWrongName_shouldSelectCorrectItemBasedOnIdNotEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        ListBox<CustomItem> listBox = new ListBox<>();
        ListBoxListDataView<CustomItem> listDataView = listBox.setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        listBox.setValue(new CustomItem(1L));

        Assertions.assertNotNull(listBox.getValue());
        Assertions.assertEquals("First", listBox.getValue().getName());

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item with an Id and the name that can be wrongly equals to
        // another items, should verify that <equals> method is not in use:
        listBox.setValue(new CustomItem(3L, "Second"));

        Assertions.assertNotNull(listBox.getValue());
        Assertions.assertEquals(Long.valueOf(3L), listBox.getValue().getId());
    }

    @Test
    void withoutSettingIdentifierProvider_setItemWithNullId_shouldSelectCorrectItemBasedOnEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        ListBox<CustomItem> listBox = new ListBox<>();
        ListBoxListDataView<CustomItem> listDataView = listBox.setItems(items);

        listBox.setValue(new CustomItem(null, "Second"));

        Assertions.assertNotNull(listBox.getValue());
        Assertions.assertEquals(Long.valueOf(2L), listBox.getValue().getId());
    }

    @Test
    void setIdentifierProviderOnId_setItemWithNullId_shouldThrowException() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        ListBox<CustomItem> listBox = new ListBox<>();
        ListBoxListDataView<CustomItem> listDataView = listBox.setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        Assertions.assertThrows(NullPointerException.class,
                () -> listBox.setValue(new CustomItem(null, "First")));
    }

    @Test
    void dataProviderListeners_listBoxAttachedAndDetached_oldDataProviderListenerRemoved() {
        DataProviderListenersTest
                .checkOldListenersRemovedOnComponentAttachAndDetach(
                        new ListBox<>(), 1, 1, new int[] { 0, 1 }, ui.getUI());
    }

    @Test
    void implementsHasTooltip() {
        Assertions.assertTrue(listBox instanceof HasTooltip);
    }

    @Test
    void implementsHasAriaLabel() {
        Assertions.assertTrue(listBox instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        listBox.setAriaLabel("aria-label");

        Assertions.assertTrue(listBox.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", listBox.getAriaLabel().get());

        listBox.setAriaLabel(null);

        Assertions.assertTrue(listBox.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        listBox.setAriaLabelledBy("aria-labelledby");

        Assertions.assertTrue(listBox.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                listBox.getAriaLabelledBy().get());

        listBox.setAriaLabelledBy(null);

        Assertions.assertTrue(listBox.getAriaLabelledBy().isEmpty());
    }

    private void assertDisabledItem(int index, boolean disabled) {
        if (disabled) {
            Assertions.assertNotNull(listBox.getElement().getChild(index)
                    .getAttribute("disabled"));
        } else {
            Assertions.assertNull(listBox.getElement().getChild(index)
                    .getAttribute("disabled"));
        }
    }
}
