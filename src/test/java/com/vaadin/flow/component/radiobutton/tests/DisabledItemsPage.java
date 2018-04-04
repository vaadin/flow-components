/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("disabled-items")
public class DisabledItemsPage extends DemoView {

    @Override
    protected void initView() {
        DisabledItemsPage();
    }

    public void DisabledItemsPage() {
        RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup();
        radioButtonGroup.setId("button-group");
        radioButtonGroup.setEnabled(false);

        NativeButton nativeButton = new NativeButton("add",
                event -> radioButtonGroup.setItems("one", "two"));
        nativeButton.setId("add-button");

        add(radioButtonGroup, nativeButton);
    }
}
