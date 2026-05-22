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
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.trigger.MenuItemClickTrigger;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.trigger.internal.SetPropertyAction;
import com.vaadin.flow.router.Route;

@Route("vaadin-context-menu/menu-item-click-trigger")
public class MenuItemClickTriggerView extends Div {

    static final String TARGET_ID = "target-div";
    static final String RESULT_ID = "result";
    static final String SET_ITEM_ID = "set-item";
    static final String CLEAR_ITEM_ID = "clear-item";
    static final String SET_MESSAGE = "Hello from trigger";

    public MenuItemClickTriggerView() {
        var target = new Div("Right-click me");
        target.setId(TARGET_ID);
        add(target);

        var result = new Div();
        result.setId(RESULT_ID);
        add(result);

        var contextMenu = new ContextMenu(target);

        var setItem = contextMenu.addItem("Set message");
        setItem.setId(SET_ITEM_ID);
        new MenuItemClickTrigger(setItem).triggers(
                new SetPropertyAction<>(result, "textContent", SET_MESSAGE));

        var clearItem = contextMenu.addItem("Clear message");
        clearItem.setId(CLEAR_ITEM_ID);
        new MenuItemClickTrigger(clearItem)
                .triggers(new SetPropertyAction<>(result, "textContent", ""));
    }
}
