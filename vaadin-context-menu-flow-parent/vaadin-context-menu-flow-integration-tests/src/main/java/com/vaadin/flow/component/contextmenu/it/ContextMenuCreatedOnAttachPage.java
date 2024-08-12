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

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-context-menu/on-attach-listener")
public class ContextMenuCreatedOnAttachPage extends Div {
    public ContextMenuCreatedOnAttachPage() {
        createContextMenuAndTarget("target-open-left-click", true);
        createContextMenuAndTarget("target-open-right-click", false);
    }

    private void createContextMenuAndTarget(String id, boolean openOnClick) {
        final Div target = new Div(new Text(id));
        target.setId(id);
        addAttachListener(ev -> {
            final ContextMenu contextMenu = new ContextMenu();
            contextMenu.addItem("ITEM");
            contextMenu.setTarget(target);
            contextMenu.setOpenOnClick(openOnClick);
            add(contextMenu);
        });
        add(target);
    }
}
