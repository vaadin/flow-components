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
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("template-button")
@Route("vaadin-button/template-button")
@JsModule("./template-button.js")
public class TemplateButtonPage extends PolymerTemplate<TemplateModel> {

    @Id("button")
    private Button templateButton;

    @Id("icon-button")
    private Button iconButton;

    public TemplateButtonPage() {
        setId("button-template");
        templateButton
                .addClickListener(event -> templateButton.setText("clicked"));
        iconButton.addClickListener(event -> iconButton.setText("clicked"));
    }
}
