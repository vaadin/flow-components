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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/clientside-filter")
public class ClientSideFilterPage extends Div {

    public ClientSideFilterPage() {
        ComboBox<String> cb = new ComboBox<>("Choose option", "Option 2",
                "Option 3", "Option 4", "Option 5");
        this.add(cb);
        cb.focus();

        this.add(new Hr());

        ComboBox<String> testBox = new ComboBox<>("Browsers");
        testBox.setItems("Google Chrome", "Mozilla Firefox", "Opera",
                "Apple Safari", "Microsoft Edge");
        this.add(testBox);

    }
}
