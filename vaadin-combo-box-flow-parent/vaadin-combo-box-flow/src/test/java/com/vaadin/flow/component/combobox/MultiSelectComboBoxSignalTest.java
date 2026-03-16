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
import org.junit.Test;

import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class MultiSelectComboBoxSignalTest extends AbstractSignalsUnitTest {

    @Test
    public void signalConstructor_setsItemsFromSignal() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("Alice");
        listSignal.insertLast("Bob");

        var comboBox = new MultiSelectComboBox<>("People", listSignal);
        ui.add(comboBox);

        List<String> items = comboBox.getGenericDataView().getItems().toList();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("Alice", items.get(0));
        Assert.assertEquals("Bob", items.get(1));
        Assert.assertEquals("People", comboBox.getLabel());

        listSignal.insertLast("Charlie");

        items = comboBox.getGenericDataView().getItems().toList();
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("Charlie", items.get(2));
    }
}
