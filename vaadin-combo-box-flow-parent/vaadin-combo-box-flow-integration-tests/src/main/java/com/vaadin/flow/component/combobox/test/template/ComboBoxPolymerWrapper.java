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
package com.vaadin.flow.component.combobox.test.template;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.templatemodel.TemplateModel;

import java.util.Arrays;

@Tag("combo-box-polymer-wrapper")
@JsModule("./src/combo-box-polymer-wrapper.ts")
public class ComboBoxPolymerWrapper
        extends PolymerTemplate<ComboBoxPolymerWrapper.Modal> {

    @Id("cb")
    private ComboBox<String> comboBox;

    public ComboBoxPolymerWrapper() {
        comboBox.setDataProvider(
                new ListDataProvider<>(Arrays.asList("B", "C", "A", "D")));
        comboBox.setValue("D");
    }

    public static class Modal implements TemplateModel {
        public Modal() {
        }
    }
}
