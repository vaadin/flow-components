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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

import elemental.json.JsonObject;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-context-menu/override-on-before-open-context-menu")
public class OverrideOnBeforeOpenContextMenuPage extends Div {

    public OverrideOnBeforeOpenContextMenuPage() {
        addContextMenuThatDoesNotOpen();
        addSeparator();
        addContextMenuThatDynamicallyChangesItems();
    }

    private void addContextMenuThatDoesNotOpen() {
        Label target = new Label("Context menu that should not open");
        target.setId("no-open-menu-target");

        ContextMenu contextMenu = new ContextMenu(target) {
            @Override
            protected boolean onBeforeOpenMenu(JsonObject eventDetail) {
                // ensure context menu will not open
                return false;
            }
        };

        contextMenu.addItem("Item 1");
        contextMenu.addItem("Item 2");
        add(target);
    }

    private void addContextMenuThatDynamicallyChangesItems() {
        Label target = new Label("Context menu that changes items dynamically");
        target.setId("dynamic-context-menu-target");

        ContextMenu contextMenu = new ContextMenu(target) {
            @Override
            protected boolean onBeforeOpenMenu(JsonObject eventDetail) {
                removeAll();

                addItem("Dynamic Item");

                return super.onBeforeOpenMenu(eventDetail);
            }
        };

        contextMenu.addItem("Item 1");
        contextMenu.addItem("Item 2");
        add(target);
    }

    private void addSeparator() {
        getElement().appendChild(new Element("hr"));
    }
}
