/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.test.template;

import java.util.Arrays;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/combo-box-in-template")
public class ComboBoxInTemplatePage extends Div {

    private Label message;

    public ComboBoxInTemplatePage() {
        message = new Label("-");
        message.setId("message");
        add(message);

        WrapperTemplate wrapper = new WrapperTemplate();
        add(wrapper);

        initCombo(wrapper.comboBoxInATemplate.getComboBox());
        initCombo(wrapper.comboBoxInATemplate2.getComboBox());

    }

    private void initCombo(ComboBox<String> combo) {
        combo.setDataProvider(
                new ListDataProvider<>(Arrays.asList("1", "2", "3")));
        combo.setValue("1");
        combo.addValueChangeListener(e -> {
            message.setText(e.getValue() == null ? "null" : e.getValue());
        });
    }

}
