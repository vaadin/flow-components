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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;

@Route("vaadin-button/icon-button")
public class IconForButtonPage extends Div {

    public IconForButtonPage() {
        Button button = new Button("Button On SlotPrefix");
        Icon icon = new Icon(VaadinIcon.BULLSEYE);
        icon.getElement().setAttribute("slot", "prefix");
        button.setIcon(icon);

        button.addClickListener(even -> button.setText("Updated text"));

        add(button);
    }
}
