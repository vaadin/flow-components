/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/virtual-child")
public class MultiSelectComboBoxVirtualChildPage extends Div {
    public MultiSelectComboBoxVirtualChildPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();

        // Add the component as a virtual child, which will initialize the
        // connector, but does not add the element to the DOM, and does not
        // finalize the Polymer element.
        // This covers scenarios like adding the component to an unopened
        // dialog, or rendering it with a Polymer or Lit template.
        getElement().appendVirtualChild(comboBox.getElement());
    }
}
