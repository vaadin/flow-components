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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/refresh-value")
public class MultiSelectComboBoxRefreshValuePage extends Div {
    public MultiSelectComboBoxRefreshValuePage() {
        MultiSelectComboBox<TestBean> comboBox = new MultiSelectComboBox<>(
                "Items");
        List<TestBean> items = IntStream.range(0, 100)
                .mapToObj(i -> new TestBean("Item " + (i + 1)))
                .collect(Collectors.toList());
        comboBox.setItems(items);
        comboBox.setItemLabelGenerator(TestBean::getName);
        // Make component wider, so that we can fit multiple chips
        comboBox.setWidth("500px");

        NativeButton changeItemLabelGenerator = new NativeButton(
                "Change item label generator", e -> {
                    comboBox.setItemLabelGenerator(
                            item -> "Custom " + item.getName());
                });
        changeItemLabelGenerator.setId("change-item-label-generator");

        NativeButton changeItemData = new NativeButton("Change item data",
                e -> {
                    items.forEach(
                            item -> item.setName("Updated " + item.getName()));
                    comboBox.getDataProvider().refreshAll();
                });
        changeItemData.setId("change-item-data");

        add(comboBox);
        add(new Div(changeItemLabelGenerator, changeItemData));
    }

    private static class TestBean {
        private String name;

        public TestBean(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
