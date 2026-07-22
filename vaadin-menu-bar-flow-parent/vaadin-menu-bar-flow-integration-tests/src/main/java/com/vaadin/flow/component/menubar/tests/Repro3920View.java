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
package com.vaadin.flow.component.menubar.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/3920
 *
 * MenuBar may render empty after: detach -> browser refresh
 * (@PreserveOnRefresh) -> reattach. The updateScheduled flag can get stuck at
 * true so resetContent() early-returns and never re-generates the items.
 */
@Route("repro-3920")
@PreserveOnRefresh
public class Repro3920View extends Div {

    private final MenuBar menuBar = new MenuBar();
    private final Div holder = new Div();

    public Repro3920View() {
        menuBar.addItem("item 1");
        menuBar.addItem("item 2");
        menuBar.setId("menu-bar");

        holder.setId("holder");
        holder.add(menuBar);

        NativeButton toggleAttached = new NativeButton("toggle attached", e -> {
            if (menuBar.getParent().isPresent()) {
                holder.remove(menuBar);
            } else {
                holder.add(menuBar);
            }
        });
        toggleAttached.setId("toggle-attached");

        // Schedules an item update AND detaches the menu bar in the SAME
        // request, so the beforeClientResponse that resets updateScheduled
        // never fires -> flag stuck true.
        NativeButton modifyAndDetach = new NativeButton("modify and detach",
                e -> {
                    menuBar.addItem("item 3");
                    holder.remove(menuBar);
                });
        modifyAndDetach.setId("modify-and-detach");

        add(toggleAttached, modifyAndDetach, new Hr(), holder);
    }
}
