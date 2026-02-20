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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.tests.MockUIRule;

public class MultiSelectComboBoxFilteringTest {
    @Rule
    public MockUIRule ui = new MockUIRule();

    @Test
    public void filter_addAndRefreshItems_doesNotToggleClientSideFiltering() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        ui.add(comboBox);

        List<String> items = new ArrayList<>(
                IntStream.range(0, 100).mapToObj(i -> "Item " + i).toList());
        comboBox.setItems(items);

        comboBox.getDataController().setViewportRange(0, 50, "foo");
        ui.fakeClientCommunication();
        Assert.assertFalse((Boolean) comboBox.getElement()
                .getPropertyRaw("_clientSideFilter"));

        items.add("foo");
        comboBox.getDataProvider().refreshAll();
        comboBox.getDataController().setViewportRange(0, 50, "");
        ui.fakeClientCommunication();
        Assert.assertFalse((Boolean) comboBox.getElement()
                .getPropertyRaw("_clientSideFilter"));
    }
}
