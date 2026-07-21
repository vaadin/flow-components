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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Repro for https://github.com/vaadin/web-components/issues/12201
 *
 * TextField with autoselect=true plus a ContextMenu: on touch devices a short
 * tap to focus the field opens the context menu (should only open on long
 * press).
 */
@Route("repro-12201")
public class Repro12201View extends Div {

    public Repro12201View() {
        // Failing case: autoselect = true
        TextField autoselectField = new TextField();
        autoselectField.setId("autoselect-field");
        autoselectField.setAutoselect(true);
        autoselectField.setValue("AAA");

        ContextMenu autoselectMenu = new ContextMenu();
        autoselectMenu.setId("autoselect-menu");
        autoselectMenu.setTarget(autoselectField);
        autoselectMenu.addItem("Item1", e -> {
        });

        // Control case: autoselect = false (default)
        TextField plainField = new TextField();
        plainField.setId("plain-field");
        plainField.setValue("BBB");

        ContextMenu plainMenu = new ContextMenu();
        plainMenu.setId("plain-menu");
        plainMenu.setTarget(plainField);
        plainMenu.addItem("Item1", e -> {
        });

        add(new Span("autoselect=true:"), autoselectField,
                new Span("autoselect=false (control):"), plainField);
    }
}
