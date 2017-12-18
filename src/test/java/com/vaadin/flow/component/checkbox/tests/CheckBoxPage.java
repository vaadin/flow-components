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
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link Checkbox}.
 *
 */
@Route("checkbox-test")
public class CheckBoxPage extends Div {

    /**
     * Creates a new instance.
     */
    public CheckBoxPage() {
        Checkbox checkbox = new Checkbox();
        checkbox.setId("checkbox");
        checkbox.addValueChangeListener(event -> {
        });
        checkbox.setValue(true);

        add(checkbox);
    }
}
