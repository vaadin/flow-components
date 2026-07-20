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

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for
 * https://github.com/vaadin/flow-components/issues/1010
 *
 * Toggling MenuItem checked state from the opened-change-listener: the change
 * should be visible the moment the menu opens, but the checkmark appears to lag
 * one open behind. The status Div shows what Java believes is checked.
 */
@Route("repro-1010")
public class Repro1010View extends Div {

    private final Map<Integer, MenuItem> menuItems = new HashMap<>();
    private int currentlyChecked = 0;

    public Repro1010View() {
        NativeButton button = new NativeButton("Say hello");
        button.setId("open-button");

        Div status = new Div();
        status.setId("status");

        ContextMenu cm = new ContextMenu(button);
        cm.setOpenOnClick(true);
        for (int i = 0; i < 10; i++) {
            final MenuItem menuItem = cm.addItem("item " + i);
            menuItem.setCheckable(true);
            menuItem.setId("item-" + i);
            menuItems.put(i, menuItem);
        }
        cm.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                currentlyChecked++;
                if (currentlyChecked >= menuItems.size()) {
                    currentlyChecked = 0;
                }
            }
            updateCurrentlyChecked();
            status.setText("Item " + currentlyChecked
                    + " should be checked: Java says "
                    + menuItems.get(currentlyChecked).isChecked());
        });
        updateCurrentlyChecked();
        status.setText("Item " + currentlyChecked + " should be checked");

        add(button, status);
    }

    private void updateCurrentlyChecked() {
        for (Map.Entry<Integer, MenuItem> entry : menuItems.entrySet()) {
            entry.getValue().setChecked(entry.getKey().equals(currentlyChecked));
        }
    }
}
