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
import com.vaadin.flow.router.Route;

/**
 * Test view for automatically attaching ContextMenu if it's not explicitly
 * attached anywhere.
 * <p>
 * Needs to be in its own view to make sure that the frontend dependencies are
 * loaded for the target component even if there are no ContextMenu components
 * attached yet to the page.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-context-menu/auto-attached-context-menu")
public class AutoAttachedContextMenuPage extends Div {

    public AutoAttachedContextMenuPage() {
        Label target = new Label(
                "Target for context menu which is automatically added to the UI");
        target.setId("target-for-not-attached-context-menu");
        ContextMenu contextMenu = new ContextMenu(target);
        contextMenu.add(new Label("Auto-attached context menu"));
        contextMenu.setId("not-attached-context-menu");

        add(target);
    }

}
