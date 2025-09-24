/*
 * Copyright 2000-2025 Vaadin Ltd.
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/client-side-filtering")
public class MultiSelectComboBoxClientSideFilteringPage extends Div {
    public MultiSelectComboBoxClientSideFilteringPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Items");
        // By generating less items than the default page size of 50 we
        // automatically enable client-side filtering
        List<String> items = IntStream.range(0, 10)
                .mapToObj(i -> "Item " + (i + 1)).collect(Collectors.toList());
        comboBox.setItems(items);

        add(comboBox);
    }
}
