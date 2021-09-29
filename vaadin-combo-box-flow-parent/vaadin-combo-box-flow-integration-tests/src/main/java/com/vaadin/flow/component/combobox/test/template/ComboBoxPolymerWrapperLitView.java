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
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.router.Route;

@Tag("combo-box-polymer-wrapper-lit-view")
@JsModule("./src/combo-box-polymer-wrapper-lit-view.ts")
@Route("vaadin-combo-box/combo-box-polymer-wrapper-lit-view")
public class ComboBoxPolymerWrapperLitView extends LitTemplate {

    @Id("cbw1")
    private ComboBoxPolymerWrapper comboBoxLitWrapper1;

    @Id("cbw2")
    private ComboBoxPolymerWrapper comboBoxLitWrapper2;

    @Id("cbw3")
    private ComboBoxPolymerWrapper comboBoxLitWrapper3;

    public ComboBoxPolymerWrapperLitView() {
    }
}
