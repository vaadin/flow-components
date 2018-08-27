/*
 * Copyright 2000-2018 Vaadin Ltd.
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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.bean.TestItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;

@Route("data-provider")
public class DataProviderPage extends Div {
    static final String COMBO_BOX_WITH_GET_ID_ID = "comboBoxWithGetId";
    static final String COMBO_BOX_WITHOUT_GET_ID_ID = "comboBoxWithoutGetId";
    static final String SET_VALUE_USING_GET_ID_BUTTON_ID = "setValueUsingGetIdButton";
    static final String SET_VALUE_USING_REFERENCE_BUTTON_ID = "setValueUsingReferenceButton";
    static final String SET_VALUE_USING_EQUALS_BUTTON_ID = "setValueUsingEqualsButton";

    public DataProviderPage() {
        List<TestItem> list = Arrays.asList(new TestItem(1, "a", "First"),
                new TestItem(2, "b", "Second"), new TestItem(3, "c", "Third"));

        ComboBox<TestItem> comboBoxWithGetId = new ComboBox<>();
        comboBoxWithGetId.setId(COMBO_BOX_WITH_GET_ID_ID);
        comboBoxWithGetId.setDataProvider(new ListDataProvider<TestItem>(list) {
            @Override
            public Object getId(TestItem item) {
                return item.getId();
            }
        });
        add(comboBoxWithGetId);

        ComboBox<TestItem> comboBoxWithoutGetId = new ComboBox<>();
        comboBoxWithoutGetId.setId(COMBO_BOX_WITHOUT_GET_ID_ID);
        comboBoxWithoutGetId.setItems(list);
        add(comboBoxWithoutGetId);

        NativeButton setValueUsingIdButton = new NativeButton(
                "Set Value Using Id",
                event -> comboBoxWithGetId.setValue(new TestItem(2)));
        setValueUsingIdButton.setId(SET_VALUE_USING_GET_ID_BUTTON_ID);
        add(setValueUsingIdButton);

        NativeButton setValueUsingReferenceButton = new NativeButton(
                "Set Value Using Reference",
                event -> comboBoxWithoutGetId.setValue(list.get(1)));
        setValueUsingReferenceButton.setId(SET_VALUE_USING_REFERENCE_BUTTON_ID);
        add(setValueUsingReferenceButton);

        NativeButton setValueUsingEqualsButton = new NativeButton(
                "Set Value Using Equals", event -> comboBoxWithoutGetId
                        .setValue(new TestItem(4, "c", "")));
        setValueUsingEqualsButton.setId(SET_VALUE_USING_EQUALS_BUTTON_ID);
        add(setValueUsingEqualsButton);
    }
}
