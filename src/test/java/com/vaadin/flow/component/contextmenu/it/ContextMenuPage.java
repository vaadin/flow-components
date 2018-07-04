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
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * Test view for {@link ContextMenu}.
 */
@Theme(Lumo.class)
@Route("context-menu-test")
public class ContextMenuPage extends Div {

    public ContextMenuPage() {
        addContextMenuWithOpenedChangeListener();
    }

    private void addContextMenuWithOpenedChangeListener() {
        Label target = new Label("Target");
        target.setId("context-menu-test");
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(target);
        Paragraph content = new Paragraph("Context menu test.");
        contextMenu.add(content);

        Div message = new Div();
        message.setId("message");

        contextMenu.addOpenedChangeListener(event -> {
            message.setText("The open state of the context menu is "
                    + contextMenu.isOpened());
        });

        add(target, contextMenu, message);

    }

}
