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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1732
 *
 * On a touch device, long-pressing the context menu target opens the menu (the
 * browser fires `contextmenu` mid-touch, which resets the gesture recognizer's
 * touchStartCoords), and then dragging without lifting the finger makes the
 * recognizer's touchmove handler dereference the nulled coords: "Cannot read
 * properties of null (reading 'x')".
 */
@Route("repro-1732")
public class Repro1732View extends Div {

    public Repro1732View() {
        Div target = new Div("Long-press here, then drag");
        target.setId("target");
        target.getStyle().set("width", "300px").set("height", "300px")
                .set("border", "1px solid black");

        ContextMenu contextMenu = new ContextMenu(target);
        contextMenu.addItem("Item 1");
        contextMenu.addItem("Item 2");

        add(target);
    }
}
