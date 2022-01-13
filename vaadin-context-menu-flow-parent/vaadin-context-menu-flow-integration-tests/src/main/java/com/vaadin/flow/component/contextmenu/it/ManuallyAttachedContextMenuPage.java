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
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-context-menu/manually-attached-context-menu")
public class ManuallyAttachedContextMenuPage extends Div {

    public ManuallyAttachedContextMenuPage() {
        Paragraph target = new Paragraph("target");
        target.setId("target");

        ContextMenu contextMenu = new ContextMenu(target);
        MenuItem item = contextMenu.addItem("foo");
        item.setCheckable(true);

        NativeButton toggleChecked = new NativeButton("toggle checked",
                e -> item.setChecked(!item.isChecked()));
        toggleChecked.setId("toggle-checked");

        // Add <vaadin-context-menu> to DOM manually
        add(target, contextMenu, toggleChecked);
    }

}
