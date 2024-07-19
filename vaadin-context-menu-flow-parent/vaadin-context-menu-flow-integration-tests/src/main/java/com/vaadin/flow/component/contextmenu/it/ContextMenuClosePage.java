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
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

@Route("vaadin-context-menu/close")
public class ContextMenuClosePage extends Div {

    public ContextMenuClosePage() {
        Paragraph target = new Paragraph("Target for context menu");
        target.setId("context-menu-target");
        add(target);

        ContextMenu contextMenu = new ContextMenu(target);
        contextMenu.addItem("Item 1");
        contextMenu.addItem("Item 2");

        NativeButton closeButton = new NativeButton("Close",
                e -> contextMenu.close());
        closeButton.setId("close-menu");
        contextMenu.add(new Div(closeButton));

        Div message = new Div();
        message.setId("closed-message");
        add(message);

        contextMenu.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                message.setText("Closed from client: " + event.isFromClient());
            }
        });
    }
}
