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
package com.vaadin.flow.component.combobox;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.dataview.ComboBoxDataView;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class ComboBoxSignalTest extends AbstractSignalsUnitTest {

    private ComboBox<String> comboBox;

    @Before
    public void setup() {
        comboBox = new ComboBox<>();
    }

    @Test
    public void bindItems_defaultItemsFilter_setsItemsFromSignal() {
        var item1Signal = new ValueSignal<>("Alice");
        var item2Signal = new ValueSignal<>("Bob");
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        ComboBoxDataView<String> dataView = comboBox.bindItems(listSignal);
        ui.add(comboBox);

        Assert.assertNotNull("Data view should not be null", dataView);
        List<String> items = dataView.getItems().toList();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("Alice", items.get(0));
        Assert.assertEquals("Bob", items.get(1));
    }

    @Test
    public void bindItems_defaultItemsFilter_updatesWhenListSignalChanges() {
        var item1Signal = new ValueSignal<>("Alice");
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        comboBox.bindItems(listSignal);
        ui.add(comboBox);

        ComboBoxDataView<String> dataView = comboBox.getGenericDataView();
        List<String> items = dataView.getItems().toList();
        Assert.assertEquals(1, items.size());

        var item2Signal = new ValueSignal<>("Bob");
        var item3Signal = new ValueSignal<>("Charlie");
        listSignal.set(List.of(item1Signal, item2Signal, item3Signal));

        items = dataView.getItems().toList();
        Assert.assertEquals(3, items.size());
    }

    @Test
    public void bindItems_defaultItemsFilter_updatesWhenItemSignalChanges() {
        var item1Signal = new ValueSignal<>("Alice");
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        comboBox.bindItems(listSignal);
        ui.add(comboBox);

        ComboBoxDataView<String> dataView = comboBox.getGenericDataView();
        List<String> items = dataView.getItems().toList();
        Assert.assertEquals("Alice", items.getFirst());

        item1Signal.set("Updated Alice");

        items = dataView.getItems().toList();
        Assert.assertEquals("Updated Alice", items.getFirst());
    }

    @Test(expected = NullPointerException.class)
    public void bindItems_nullSignal_throws() {
        comboBox.bindItems(null);
    }

    @Test
    public void bindItems_customItemsFilter_setsItemsFromSignal() {
        var item1Signal = new ValueSignal<>("Alice");
        var item2Signal = new ValueSignal<>("Bob");
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        ComboBoxDataView<String> dataView = comboBox.bindItems(listSignal,
                filter -> item -> item.toLowerCase()
                        .contains(filter.toLowerCase()));
        ui.add(comboBox);

        Assert.assertNotNull("Data view should not be null", dataView);
        List<String> items = dataView.getItems().toList();
        Assert.assertEquals(2, items.size());
    }

    @Test
    public void signalConstructor_setsItemsFromSignal() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("Alice");
        listSignal.insertLast("Bob");

        comboBox = new ComboBox<>("People", listSignal);
        ui.add(comboBox);

        ComboBoxDataView<String> dataView = comboBox.getGenericDataView();
        List<String> items = dataView.getItems().toList();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("Alice", items.get(0));
        Assert.assertEquals("Bob", items.get(1));
        Assert.assertEquals("People", comboBox.getLabel());

        listSignal.insertLast("Charlie");

        items = dataView.getItems().toList();
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("Charlie", items.get(2));
    }
}
