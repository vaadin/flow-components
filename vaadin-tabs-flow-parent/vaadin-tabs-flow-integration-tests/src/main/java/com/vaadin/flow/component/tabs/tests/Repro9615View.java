/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.tabs.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;

/**
 * Repro for https://github.com/vaadin/flow-components/issues/9615 — TabSheet in
 * a Dialog bleeds out on mobile screens.
 */
@Route("repro-9615")
public class Repro9615View extends Div {

    public Repro9615View() {
        Button button = new Button("TabSheet issue");
        button.setId("open-dialog");

        button.addClickListener(e -> {
            Dialog dialog = new Dialog();

            TabSheet tabSheet = new TabSheet();
            for (int i = 0; i < 10; i++) {
                tabSheet.add(new Tab("Tab" + i), new Div(
                        "Lorem Ipsum Lorem Ipsum Lorem Ipsum Lorem Ipsum Lorem Ipsum Lorem Ipsum Lorem Ipsum Lorem Ipsum Lorem Ipsum"));
            }

            dialog.add(tabSheet);
            dialog.open();
        });

        add(button);
    }
}
