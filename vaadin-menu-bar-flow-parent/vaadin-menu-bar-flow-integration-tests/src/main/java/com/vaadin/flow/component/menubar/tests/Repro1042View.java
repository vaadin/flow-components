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

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1042
 * <p>
 * A menu item that is hidden at the time the client-side items array is first
 * generated permanently loses its sub menu: it is rendered as a plain item /
 * button with no children once it is made visible again.
 */
@Route("repro-1042")
public class Repro1042View extends Div {

    public Repro1042View() {
        add(nested(), rootHidden(), control());
    }

    /**
     * The issue's own example: a sub menu item and its sub-sub menu item are
     * both hidden initially. Making them visible again is expected to restore
     * the sub-sub menu, but "Sub 2" ends up without children.
     */
    private Div nested() {
        MenuBar menuBar = new MenuBar();
        menuBar.setId("nested-bar");
        menuBar.addItem("Dummy");
        MenuItem rootItem = menuBar.addItem("Root");

        var subMenu = rootItem.getSubMenu();
        MenuItem sub1 = subMenu.addItem("Sub 1");
        MenuItem subSub1 = sub1.getSubMenu().addItem("Sub sub 1");

        MenuItem sub2 = subMenu.addItem("Sub 2");
        MenuItem subSub2 = sub2.getSubMenu().addItem("Sub sub 2");
        sub2.setVisible(false);
        subSub2.setVisible(false);

        NativeButton toggle1 = new NativeButton("toggle 1", event -> {
            subSub1.setVisible(!subSub1.isVisible());
            sub1.setVisible(subSub1.isVisible());
            rootItem.setVisible(sub1.isVisible() || sub2.isVisible());
        });
        toggle1.setId("nested-toggle-1");

        NativeButton toggle2 = new NativeButton("toggle 2", event -> {
            subSub2.setVisible(!subSub2.isVisible());
            sub2.setVisible(subSub2.isVisible());
            rootItem.setVisible(sub1.isVisible() || sub2.isVisible());
        });
        toggle2.setId("nested-toggle-2");

        return new Div(new H3("Nested: sub + sub-sub hidden initially"),
                menuBar, toggle1, toggle2);
    }

    /**
     * Root-level variant: the root item has a sub menu and is hidden initially.
     * Making it visible brings the button back (fixed in #5539) but the sub
     * menu stays empty.
     */
    private Div rootHidden() {
        MenuBar menuBar = new MenuBar();
        menuBar.setId("root-hidden-bar");
        MenuItem rootItem = menuBar.addItem("Root hidden");
        rootItem.getSubMenu().addItem("Sub 1");
        rootItem.getSubMenu().addItem("Sub 2");
        rootItem.setVisible(false);

        NativeButton toggle = new NativeButton("toggle",
                event -> rootItem.setVisible(!rootItem.isVisible()));
        toggle.setId("root-hidden-toggle");

        // Any structural change runs MenuBar.resetContent(), which regenerates
        // the items array from the server and restores the sub menu.
        NativeButton forceReset = new NativeButton("force reset", event -> {
            MenuItem temp = menuBar.addItem("temp");
            menuBar.remove(temp);
        });
        forceReset.setId("root-hidden-force-reset");

        return new Div(new H3("Root item hidden initially"), menuBar, toggle,
                forceReset);
    }

    /**
     * Control: the same root item, but visible when the items array is first
     * generated. Hiding and showing it keeps the sub menu, which isolates
     * "hidden at first render" as the trigger.
     */
    private Div control() {
        MenuBar menuBar = new MenuBar();
        menuBar.setId("control-bar");
        MenuItem rootItem = menuBar.addItem("Root visible");
        rootItem.getSubMenu().addItem("Sub 1");
        rootItem.getSubMenu().addItem("Sub 2");

        NativeButton toggle = new NativeButton("toggle",
                event -> rootItem.setVisible(!rootItem.isVisible()));
        toggle.setId("control-toggle");

        return new Div(new H3("Control: root item visible initially"), menuBar,
                toggle);
    }
}
